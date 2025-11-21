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

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer httpStatus;   // 성공 때 없음, 실패 때 있음

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String customCode; // 성공할 때 없음, 실패 때 있음, 커스텀 예외땐 예외 코드 아니면 0000

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

  public static <T> ApiResponse<T> error(String message, String customCode, int httpStatus) {
    return ApiResponse.<T>builder()
      .status(ERROR)
      .httpStatus(httpStatus)
      .customCode(customCode)
      .errorMessage(message)
      .build();
  }
}
