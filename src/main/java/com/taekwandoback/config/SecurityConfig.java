package com.taekwandoback.config;

import com.taekwandoback.jwt.JwtAuthenticationFilter;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
            .cors(httpSecurityCorsConfigurer -> {
                httpSecurityCorsConfigurer.configurationSource(
                    corsConfigurationSource());  // CORS 설정 적용
            })
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(httpSecurityHttpBasicConfigurer -> httpSecurityHttpBasicConfigurer.disable())
            .sessionManagement(httpSecuritySessionManagementConfigurer ->
                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
                authorizationManagerRequestMatcherRegistry.requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/members/**",
                    "/admin/**"
                ).permitAll();
                authorizationManagerRequestMatcherRegistry.anyRequest().authenticated();
            })
            .addFilterAfter(jwtAuthenticationFilter, CorsFilter.class)
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));  // 허용할 도메인
        configuration.setAllowedMethods(
            Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // 모든 경로에 CORS 설정 적용
        return source;
    }
}
