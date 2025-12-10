package gighub.worketserver.service;

import gighub.worketserver.global.config.RestTemplateConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OcrService {

  @Value("${naver.ocr.url}")
  private String ocrApiUrl;

  @Value("${naver.ocr.secret}")
  private String ocrSecret;

  public String processOcr(MultipartFile imageFile, String message) {
    RestTemplateConfig restTemplate = new RestTemplateConfig();

    try {
      HttpHeaders headers = new HttpHeaders();
      headers.set("X-OCR-SECRET", ocrSecret);
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add("message", message);

      ByteArrayResource fileResource = new ByteArrayResource(imageFile.getBytes()) {
        @Override
        public String getFilename() {
          return imageFile.getOriginalFilename();
        }
      };

      body.add("file", fileResource);

      HttpEntity<MultiValueMap<String, Object>> requestEntity =
        new HttpEntity<>(body, headers);


      ResponseEntity<String> response = restTemplate.restTemplate().exchange(
        ocrApiUrl,
        HttpMethod.POST,
        requestEntity,
        String.class
      );
      return response.getBody();

    } catch (Exception e) {
      throw new RuntimeException("OCR 호출 실패: " + e.getMessage());
    }
  }
}
