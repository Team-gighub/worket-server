package gighub.worketserver.global.security.config;

import gighub.worketserver.global.filter.TokenAuthenticationFilter;
import gighub.worketserver.global.security.handler.CustomAuthorizationRequestResolver;
import gighub.worketserver.global.security.handler.OAuth2SuccessHandler;
import gighub.worketserver.global.security.service.CustomOAuth2UserService;
import gighub.worketserver.repository.CustomAuthorizationRequestRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class CustomSecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final TokenAuthenticationFilter tokenAuthenticationFilter;

  @Bean
  public SecurityFilterChain filterChain(
    HttpSecurity http,
    CustomAuthorizationRequestResolver customAuthorizationRequestResolver,
    CustomAuthorizationRequestRepository customAuthorizationRequestRepository // ✅ 메서드 인자로만 받기
  ) throws Exception {

    http
      .cors(Customizer.withDefaults())
      .csrf(csrf -> csrf.disable())
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .formLogin(form -> form.disable())
      .httpBasic(basic -> basic.disable())

      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/oauth2/**").permitAll()
        .anyRequest().authenticated()
      )

      .oauth2Login(oauth -> oauth
        .authorizationEndpoint(auth -> auth
          .authorizationRequestResolver(customAuthorizationRequestResolver)
          .authorizationRequestRepository(customAuthorizationRequestRepository) // ✅ 여기에만 사용
        )
        .userInfoEndpoint(user -> user.userService(customOAuth2UserService))
        .successHandler(oAuth2SuccessHandler)
      )

      .exceptionHandling(ex -> ex
        .authenticationEntryPoint((request, response, authException) -> {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        })
      )

      .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("http://localhost:3000"));
    config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setExposedHeaders(List.of("Authorization"));
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
