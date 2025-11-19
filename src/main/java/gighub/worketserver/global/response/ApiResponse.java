package gighub.worketserver.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ApiResponse<T> {

  private static final String SUCCESS = "success";
  private static final String ERROR = "error";

  private String status;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String customCode;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T data;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String errorMessage;


  public static <T> ApiResponse<T> ok(T data) {
    return ApiResponse.<T>builder()
      .status(SUCCESS)
      .data(data)
      .build();
  }

  public static <T> ApiResponse<T> error(String errorMessage) {
    return ApiResponse.<T>builder()
      .status(ERROR)
      .errorMessage(errorMessage)
      .build();
  }

  // 특정 code를 반환해주고 싶다면
  public static <T> ApiResponse<T> error(String errorMessage, String customCode) {
    return ApiResponse.<T>builder()
      .status(ERROR)
      .customCode(customCode)
      .errorMessage(errorMessage)
      .build();
  }
}
