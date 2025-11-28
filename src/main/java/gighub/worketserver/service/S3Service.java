package gighub.worketserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import gighub.worketserver.global.exception.CommonErrorCode;
import gighub.worketserver.global.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

import software.amazon.awssdk.core.client.builder.SdkDefaultClientBuilder;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.utils.IoUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;
  @Value("${s3.bucket-api-url}")
  private String s3BucketUrl;
  private SdkHttpClient sdkHttpClient = ApacheHttpClient.create();

  /**
   * 계약서 관련 파일들을 업로드하기 위한 uploadContractFile 함수
   *
   * @param fileName    contractId에 따른 폴더 경로를 포함한 파일명을 입력합니다.
   * @param contentType 업로드하고자 하는 미디어의 형식을 입력합니다.
   * @param content     업로드하고자 하는 미디어를 byte로 바꾸어 입력합니다.
   *
   */
  public String uploadContractFile(byte[] content, String fileName, String contentType) throws NoSuchAlgorithmException, IOException {


    // MD5 Base64 계산
    MessageDigest md = MessageDigest.getInstance("MD5");
    byte[] md5Bytes = md.digest(content);
    String md5Base64 = Base64.getEncoder().encodeToString(md5Bytes);

    // Presigned URL 발급 요청
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(s3BucketUrl + "/getTempPresignedUrl")
      .queryParam("filename", fileName)
      .queryParam("contentType", contentType)
      .queryParam("md5", md5Base64);

    ResponseEntity<Map> response = restTemplate
      .exchange(builder.build(false).toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), Map.class);
    String presignedUrlString = (String) response.getBody().get("url");

    log.info("presignedUrl Success! Status: {}", presignedUrlString );

    // PUT 요청 생성
    SdkHttpRequest.Builder requestBuilder = SdkHttpRequest.builder()
      .uri(URI.create(presignedUrlString))
      .method(SdkHttpMethod.PUT);

    requestBuilder.putHeader("Content-Type", contentType);
    requestBuilder.putHeader("Content-MD5", md5Base64);
    SdkHttpRequest request = requestBuilder.build();

    // PUT 요청 실행
    HttpExecuteRequest executeRequest = HttpExecuteRequest.builder()
      .request(request)
      .contentStreamProvider(() -> new ByteArrayInputStream(content))
      .build();

    log.info("Headers before sending: {}", executeRequest.httpRequest().headers());

    // 전송 및 결과 확인
    HttpExecuteResponse executeResponse = sdkHttpClient.prepareRequest(executeRequest).call();
    int statusCode = executeResponse.httpResponse().statusCode();

    if (statusCode == 200) {
      log.info("{} S3 Upload Success! Status: {}", fileName ,statusCode);
      return presignedUrlString.split("\\?")[0];
    } else {
      // 실패 시 응답 본문 읽기 (에러 메시지 확인용)
      String errorBody = executeResponse.responseBody()
        .map(stream -> {
          try {
            return IoUtils.toUtf8String(stream);
          } catch (Exception e) {
            return "Cannot read body";
          }
        })
        .orElse("No Body");

      log.error("{} S3 Upload Failed. Status: {}", fileName, statusCode);
      log.error("Error Body: {}", errorBody);
      throw new RuntimeException("S3 업로드 실패. 응답 코드: " + statusCode);
    }
  }

  /**
   * 파일 조회/다운로드를 위한 getPresignedUrl 함수
   *
   * @param bucketName S3에 존재하는 버킷의 이름을 전달합니다.( s3-worket-bucket: 서명 파일, 계약서 임시 저장 폴더 존재, worket-contract-immutable: 계약서 최종 저장 버킷)
   * @param url   저장된 url
   */
  public String getPresignedUrl(String bucketName, String url) throws JsonProcessingException, URISyntaxException {
    URI uri = new URI(url);
    String fileName = uri.getPath();

    // 맨 앞의 슬래시 제거
    if (fileName.startsWith("/")) {
      fileName = fileName.substring(1);
    }

    // Presigned URL 발급 요청
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(s3BucketUrl + "/getDownloadPresignedUrl")
      .queryParam("bucket", bucketName)
      .queryParam("filename", fileName);

    ResponseEntity<Map> response = restTemplate.exchange(builder.build(false).toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), Map.class);

    String presignedUrlString = (String) response.getBody().get("url");
    return presignedUrlString;
  }

  /**
   * 전체 URL에서 {contractId}까지만 반환
   * <p>
   * 예시:
   * input: https://bucket.s3.amazonaws.com/12345/file.pdf
   * output: https://bucket.s3.amazonaws.com/12345/
   */
  public String extractContractPath(String fullUrl) {
    if (fullUrl == null || fullUrl.isEmpty()) {
      return "";
    }

    // 쿼리스트링 제거
    String urlWithoutQuery = fullUrl.split("\\?")[0];

    // 마지막 '/' 기준으로 잘라서 contractId까지만 반환
    int lastSlashIndex = urlWithoutQuery.lastIndexOf('/');
    if (lastSlashIndex == -1) {
      return urlWithoutQuery; // 슬래시 없으면 전체 반환
    }

    return urlWithoutQuery.substring(0, lastSlashIndex + 1);
  }

  /**
   * 정산 완료 후, 최종적으로 컴플라이언스 모드로 업로드 요청하는 함수
   *
   * @param folderName 업로드되어야 하는 contractId (폴더명)
   * @throws JsonProcessingException
   * @throws UnsupportedEncodingException
   */
  public void finalizeContractUpload(String folderName) throws JsonProcessingException, UnsupportedEncodingException {
    String decodedFolderName = URLDecoder.decode(folderName, StandardCharsets.UTF_8.toString());

    // Presigned URL 발급 요청
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(s3BucketUrl + "/finalizeContractUpload")
      .queryParam("filename", decodedFolderName);

    // POST 요청 & 응답
    ResponseEntity<Map> response = restTemplate.exchange(builder.build(false).toUriString(), HttpMethod.POST, new HttpEntity<>(new HttpHeaders()), Map.class);
    Map<String, Object> responseBody = response.getBody();
    if (responseBody == null) {
      log.error("FinalizeUpload FAILED: Response body is null");
      throw new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR, "응답 본문이 존재하지 않습니다.");
    }

    int statusCode = (int) responseBody.get("statusCode");
    String bodyString = (String) responseBody.get("body");

    JsonNode json;
    try {
      json = objectMapper.readTree(bodyString);
    } catch (JsonProcessingException e) {
      log.error("JSON 파싱 실패: {}", e.getMessage(), e);
      throw new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR, "응답 JSON 파싱에 실패했습니다.");
    }

    // 로그 (성공/실패)
    if (statusCode == HttpStatus.OK.value()) {
      log.info("FinalizeUpload SUCCESS: message={}, folder={}, fileCount={}",
        json.get("message").asText(),
        json.path("folder").asText(null),
        json.path("fileCount").asInt(0)
      );
    } else {
      log.warn("FinalizeUpload FAILED: statusCode={}, message={}",
        statusCode,
        json.path("message").asText()
      );
    }
  }


}
