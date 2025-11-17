package gighub.worketserver.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

// Spring Boot DTO (예시)
public record GeminiRequestDto(List<Content> contents, @JsonProperty("generationConfig")
Map<String, Object> generationConfig) {
  public record Content(String role, List<Part> parts) {
  } // <--- content와 role, parts

  public record Part(String text) {
  } // <--- text
}
