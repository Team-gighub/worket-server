package gighub.worketserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
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

  @Value("${s3.bucket-api-url}")
  private String s3BucketUrl;
  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;
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
    String presignedUrlString = objectMapper.readTree((String) response.getBody().get("body")).get("url").asText();

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

    // 전송 및 결과 확인
    HttpExecuteResponse executeResponse = sdkHttpClient.prepareRequest(executeRequest).call();
    int statusCode = executeResponse.httpResponse().statusCode();

    if (statusCode == 200) {
      log.info("S3 Upload Success! Status: {}", statusCode);
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

      log.error("S3 Upload Failed. Status: {}", statusCode);
      log.error("Error Body: {}", errorBody);
      throw new RuntimeException("S3 업로드 실패. 응답 코드: " + statusCode);
    }
  }

  /**
   * 파일 조회/다운로드를 위한 getPresignedUrl 함수
   *
   * @param bucketName S3에 존재하는 버킷의 이름을 전달합니다.( s3-worket-bucket: 서명 파일, 계약서 임시 저장 폴더 존재, worket-contract-immutable: 계약서 최종 저장 버킷)
   * @param fileName   contractID를 포함하는 경로와 함께 서명 파일명을 인자로 전달합니다.
   *                   예시: {directoryName}/{contractId}/{signer}_signature_{timestamp}.png
   *                   1) contracts-temp: 계약서 관련 파일들이 업로드되는 디렉토리
   *                   2) signatures: 서명 파일들이 업로드되는 디렉토리
   */
  public String getPresignedUrl(String bucketName, String fileName) throws JsonProcessingException {
    // Presigned URL 발급 요청
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(s3BucketUrl + "/getDownloadPresignedUrl")
      .queryParam("bucket", bucketName)
      .queryParam("filename", fileName);

    ResponseEntity<Map> response = restTemplate.exchange(builder.build(false).toUriString(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), Map.class);

    String presignedUrlString = objectMapper.readTree((String) response.getBody().get("body")).get("url").asText();
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


}
