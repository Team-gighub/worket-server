package gighub.worketserver.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gighub.worketserver.dto.GeminiRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class GeminiService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Value("${GEMINI_SECRET}")
  private String geminiApiKey;
  @Value("${GEMINI_MODEL}")
  private String modelName;
  @Value("${GEMINI_URL}")
  private String baseUrl;


  public String getRawGeminiResponse(String ocrJsonString) {

    // 1. JSON 문자열을 Map으로 변환하여 파싱 로직에 전달
    Map<String, Object> ocrJsonMap;
    try {
      ocrJsonMap = objectMapper.readValue(ocrJsonString, new TypeReference<Map<String, Object>>() {
      });
    } catch (JsonProcessingException e) {
      throw new RuntimeException("OCR JSON 역직렬화 오류", e);
    }

    // 2. JSON Map에서 필요한 텍스트만 추출, 프롬프트 구성
    String readableOcrText = extractReadableText(ocrJsonMap);
    String prompt = buildPrompt(readableOcrText);

    // 3. Gemini API 호출 설정
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    GeminiRequestDto requestBody = buildGeminiRequestBody(prompt);

    String geminiUrl = baseUrl + modelName.trim() + ":generateContent?key=" + geminiApiKey;

    // 4. RestTemplate 호출 시 반환 타입을 String으로 지정
    ResponseEntity<JsonNode> geminiResponse = restTemplate.postForEntity(
      geminiUrl,
      new HttpEntity<>(requestBody, headers),
      JsonNode.class
    );

    JsonNode responseNode = geminiResponse.getBody();

    // 경로: candidates -> [0] -> content -> parts -> [0] -> text
    JsonNode textNode = responseNode
      .path("candidates").get(0)
      .path("content").path("parts").get(0)
      .path("text");

    if (textNode.isTextual()) {
      String rawJsonStringWithMarkdown = textNode.asText();
      //Markdown으로 온 형태 가공
      String pureJsonString = cleanGeminiJsonOutput(rawJsonStringWithMarkdown);
      return pureJsonString; // ⭐️ 최종적으로 추출된 텍스트(JSON 문자열)만 반환
    } else {
      throw new RuntimeException("Gemini 응답에서 'text' 필드를 찾을 수 없거나 형식이 잘못되었습니다.");
    }

    //
  }

  private String cleanGeminiJsonOutput(String codeBlockText) {
    // 1. ```json 또는 ```을 포함 찾기
    Pattern pattern = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(codeBlockText);

    if (matcher.find()) {
      // 정규식의 캡처 그룹 1 (순수한 JSON 객체 {...})를 반환
      return matcher.group(1);
    }

    // 만약 모델이 Markdown을 사용하지 않고 JSON만 반환->trim만 수행
    return codeBlockText.trim();
  }

  //ocr 결과 json에서 필요 field를 가져오는 과정
  private String extractReadableText(Map<String, Object> jsonMap) {
    StringBuilder fullText = new StringBuilder();

    // "images" 리스트를 가져옵니다.
    List<Map<String, Object>> images = (List<Map<String, Object>>) jsonMap.getOrDefault("images", List.of());

    for (Map<String, Object> pageData : images) {

      List<Map<String, Object>> fields = (List<Map<String, Object>>) pageData.getOrDefault("fields", List.of());

      // 페이지 시작을 표시
      fullText.append("--- 페이지 내용 ---\n");

      for (Map<String, Object> field : fields) {
        String text = (String) field.get("inferText");
        Boolean lineBreak = (Boolean) field.getOrDefault("lineBreak", false);

        if (text != null && !text.isBlank()) {
          // 텍스트 앞뒤 공백 제거
          String trimmedText = text.trim();

          fullText.append(trimmedText);

          // lineBreak가 true이면 줄바꿈을 추가
          if (lineBreak) {
            fullText.append("\n");
          } else {
            // 아니면 일반적인 공백 추가
            fullText.append(" ");
          }
        }
      }
      fullText.append("\n"); // 페이지 끝에 추가적인 줄바꿈
    }
    // 전체 텍스트 반환
    return fullText.toString().trim();
  }

  // 제미나이에게 줄 prompt
  private String buildPrompt(String ocrText) {
    String format = "{\n" +
      // 1. contractInfo 객체
      "        \"contractInfo\": {\n" +
      "            \"title\": \"[계약서의 제목 또는 계약명]\",\n" +
      "            \"amount\": \"[총 계약 금액]\",\n" +
      "            \"startDate\": \"[계약 시작일]\",\n" +
      "            \"endDate\": \"[계약 종료일]\"\n" +
      "        },\n" +

      // 2. clientInfo 객체
      "        \"clientInfo\": {\n" +
      "            \"name\": \"[갑의 성명]\",\n" +
      "            \"phone\": \"[갑의 연락처]\"\n" +
      "        },\n" +

      // 3. freelancerInfo 객체
      "        \"freelancerInfo\": {\n" +
      "            \"name\": \"[을의 성명]\",\n" +
      "            \"phone\": \"[을의 연락처]\",\n" +
      "            \"account\": \"[을의 계좌번호]\",\n" +
      "            \"bank\": \"[을의 은행]\"\n" +
      "        }\n" +

      "}";
    String systemInstruction = "\"당신은 문서에서 핵심 정보를 **정확하게 추출**하는 AI 전문가입니다. \"\n" +
      "    \"**절대로** 주어진 OCR 텍스트에 **존재하지 않는 값**을 임의로 만들어서는 안 됩니다. \"\n" +
      "    \"오직 텍스트 내의 정보만 사용하고, 추출할 수 없는 항목은 'N/A'로 표시하세요.\"\n" +
      "    \"출력은 **반드시** 아래 요청된 포맷을 따라야 합니다. \"" +
      "    \"amount 금액에서 만원부분은 빼줘서 넘겨주고 날짜 형태는 2024-05-12 처럼 년/월/일이 아니라 -로 넘기세요. \"";

    return String.format(
      "%s\n\n다음 OCR JSON을 분석하여, 각 항목을 '|' (파이프 문자)로 구분하고, '%s' 형태로 단 한 줄로 출력하세요.\n---\nOCR JSON: %s",
      systemInstruction, format, ocrText
    );
  }

  //최종 gemini가 알아들을 수 있게 request
  private GeminiRequestDto buildGeminiRequestBody(String prompt) {
    return new GeminiRequestDto(
      List.of(
        new GeminiRequestDto.Content("user", List.of(new GeminiRequestDto.Part(prompt)))
      ),
      // Map.of("temperature", 0.0)을 generationConfig 필드에 할당하도록 DTO가 수정되었음을 가정 ⭐️
      Map.of("temperature", 0.0)
    );
  }
}
