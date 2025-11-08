package gighub.worketserver.user;

import gighub.worketserver.auth.token.TokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;      // ✅ GET 요청
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;        // ✅ status, jsonPath 등
import static org.springframework.http.HttpHeaders.AUTHORIZATION;                        // ✅ Authorization 헤더
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.junit.jupiter.api.Assertions.*;



@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TokenProvider tokenProvider;


  @Test
  void findAll() {
  }

  @Test
  @DisplayName("마이페이지 조회")
  void getMyPageSuccess() throws Exception {
    // given
    var authorities = List.of(new SimpleGrantedAuthority("ROLE_FREELANCER"));
    Authentication auth = new UsernamePasswordAuthenticationToken("1234567890", null, authorities);
    String accessToken = tokenProvider.generateAccessToken(auth);

    // when & then
    mockMvc.perform(get("/mypage")
        .header(AUTHORIZATION, "Bearer " + accessToken)
        .contentType(APPLICATION_JSON))
      .andExpect(status().isOk());
  }

  @Test
  void updateUser() {
  }
}
