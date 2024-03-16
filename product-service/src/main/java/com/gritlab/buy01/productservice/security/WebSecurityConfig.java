package com.gritlab.buy01.productservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.gritlab.buy01.productservice.security.jwt.AuthEntryPointJwt;
import com.gritlab.buy01.productservice.security.jwt.AuthTokenFilter;
import com.gritlab.buy01.productservice.service.KafkaService;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter(KafkaService kafkaService) {
    return new AuthTokenFilter(kafkaService);
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http, AuthEntryPointJwt unauthorizedHandler, KafkaService kafkaService)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.GET, "/api/productHealth")
                    .permitAll()
                    .requestMatchers("/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated());

    http.addFilterBefore(
        authenticationJwtTokenFilter(kafkaService), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
