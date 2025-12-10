package gighub.worketserver.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import gighub.worketserver.global.exception.*;
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
  public void commence(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationException authException) throws IOException {

    Object ex = request.getAttribute("exception");

    int status;
    String message;
    String code;

    if (ex instanceof TokenErrorCode token) {
      status = token.getHttpStatus().value();
      message = token.getMessage();
      code = token.getCustomCode();
    } else if (ex instanceof PasscodeForFilterErrorCode pass) {
      status = pass.getHttpStatus().value();
      message = pass.getMessage();
      code = pass.getCustomCode();

    } else if (ex instanceof ProfileErrorCode profile) {
      status = profile.getHttpStatus().value();
      message = profile.getMessage();
      code = profile.getCustomCode();

    } else {
      // 둘 다 아니면 기본값
      status = 401;
      message = authException.getMessage() != null
        ? authException.getMessage()
        : "인증이 필요합니다.";
      code = "UNAUTHORIZED";
    }

    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    ApiResponse<Void> body = ApiResponse.error(message, code, status);

    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
