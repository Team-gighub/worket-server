package gighub.worketserver.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import gighub.worketserver.global.exception.ErrorCode;
import gighub.worketserver.global.exception.PasscodeException;
import gighub.worketserver.global.exception.TokenErrorCode;
import gighub.worketserver.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationException authException) throws IOException {

    // 1) 필터에서 미리 실어둔 에러코드가 있는지 확인
    Object attr = request.getAttribute("exception");
    ErrorCode errorCode = null;

    if (attr instanceof ErrorCode e) {
      errorCode = e;
    }

    // 2) 없으면 authException 타입으로 판단
    if (errorCode == null && authException instanceof PasscodeException pe) {
      errorCode = pe.getErrorCode();
    }

    // 3) 그래도 없으면 기본값
    if (errorCode == null) {
      errorCode = TokenErrorCode.INVALID_TOKEN;
    }

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.setStatus(errorCode.getHttpStatus().value());

    ApiResponse<Void> errorResponse =
      ApiResponse.error(errorCode.getMessage(), errorCode.getCode());

    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
