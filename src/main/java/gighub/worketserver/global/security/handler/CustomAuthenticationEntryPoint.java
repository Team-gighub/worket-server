package gighub.worketserver.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
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
  public void commence(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationException authException) throws IOException {

    // Filter에서 저장한 예외 정보 가져오기
    TokenErrorCode errorCode = (TokenErrorCode) request.getAttribute("exception");

    // 예외 정보가 없으면 기본값
    if (errorCode == null) {
      errorCode = TokenErrorCode.INVALID_TOKEN;
    }

    // 응답 설정
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.setStatus(errorCode.getHttpStatus().value());

    ApiResponse<Void> errorResponse = ApiResponse.error(errorCode.getMessage());

    // JSON으로 변환해서 응답
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
