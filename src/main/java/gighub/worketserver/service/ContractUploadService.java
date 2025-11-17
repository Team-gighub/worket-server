package gighub.worketserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gighub.worketserver.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContractUploadService {

  private final OcrService ocrService;
  private final GeminiService geminiService;
  private final ObjectMapper objectMapper;

  public ApiResponse<?> process(MultipartFile file, String message) {

    try {
      // 1. OCR 실행
      String ocrJson = ocrService.processOcr(file, message);

      // 2. LLM 실행 (OCR을 통해 받아온 값을 넘겨줌)
      String llmJson = geminiService.getRawGeminiResponse(ocrJson);

      // 3. JSON → Map 변환
      Map<String, Object> result = objectMapper.readValue(
        llmJson,
        new TypeReference<Map<String, Object>>() {
        }
      );

      return ApiResponse.ok(result);

    } catch (Exception e) {
      // 에러 응답
      return ApiResponse.error("계약서 처리 중 오류: " + e.getMessage());
    }
  }
}
