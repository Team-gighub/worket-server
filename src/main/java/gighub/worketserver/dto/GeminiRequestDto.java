package gighub.worketserver.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

// Gemini API 요청 DTO
public record GeminiRequestDto(List<Content> contents, @JsonProperty("generationConfig")
Map<String, Object> generationConfig) {
  public record Content(String role, List<Part> parts) {
  } // <--- content와 role, parts

  public record Part(String text) {
  } // <--- text
}
