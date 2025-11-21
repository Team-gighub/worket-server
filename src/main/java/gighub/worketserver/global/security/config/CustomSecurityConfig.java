package gighub.worketserver.global.security.config;

import gighub.worketserver.global.filter.PasscodeCheckFilter;
import gighub.worketserver.global.filter.ProfileCheckFilter;
import gighub.worketserver.global.filter.TokenAuthenticationFilter;
import gighub.worketserver.global.security.converter.KakaoTokenResponseConverter;
import gighub.worketserver.global.security.handler.CustomAccessDeniedHandler;
import gighub.worketserver.global.security.handler.CustomAuthenticationEntryPoint;
import gighub.worketserver.global.security.resolver.CustomAuthorizationRequestResolver;
import gighub.worketserver.global.security.handler.OAuth2SuccessHandler;
import gighub.worketserver.global.security.service.CustomOAuth2UserService;
import gighub.worketserver.global.security.repository.CustomAuthorizationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class CustomSecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final TokenAuthenticationFilter tokenAuthenticationFilter;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;
  private final PasscodeCheckFilter passcodeCheckFilter;
  private final ProfileCheckFilter profileCheckFilter;

  @Bean
  public SecurityFilterChain filterChain(
    HttpSecurity http,
    CustomAuthorizationRequestResolver customAuthorizationRequestResolver,
    CustomAuthorizationRequestRepository customAuthorizationRequestRepository
  ) throws Exception {

    http
      .cors(Customizer.withDefaults())
      .csrf(csrf -> csrf.disable())
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .formLogin(form -> form.disable())
      .httpBasic(basic -> basic.disable())

      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/oauth2/**").permitAll()
        .requestMatchers("/auth/token/**").permitAll()
        .requestMatchers("/test").permitAll()
        .anyRequest().authenticated()
      )

      .oauth2Login(oauth -> oauth
        .authorizationEndpoint(auth -> auth
          .authorizationRequestResolver(customAuthorizationRequestResolver)
          .authorizationRequestRepository(customAuthorizationRequestRepository)
        )
        .tokenEndpoint(token -> token.accessTokenResponseClient(kakaoTokenResponseClient()))
        .userInfoEndpoint(user -> user.userService(customOAuth2UserService))
        .successHandler(oAuth2SuccessHandler)
      )

      .exceptionHandling(ex -> ex
        .authenticationEntryPoint(customAuthenticationEntryPoint)
        .accessDeniedHandler(customAccessDeniedHandler)
      )

      .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
      .addFilterAfter(passcodeCheckFilter, AuthorizationFilter.class)
      .addFilterAfter(profileCheckFilter, AuthorizationFilter.class);

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

  private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> kakaoTokenResponseClient() {
    DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();

    OAuth2AccessTokenResponseHttpMessageConverter converter =
      new OAuth2AccessTokenResponseHttpMessageConverter();
    converter.setAccessTokenResponseConverter(new KakaoTokenResponseConverter());

    RestTemplate restTemplate = new RestTemplate(Arrays.asList(
      new FormHttpMessageConverter(), converter));
    restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

    client.setRestOperations(restTemplate);
    return client;
  }
}
