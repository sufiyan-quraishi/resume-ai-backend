package com.fortunecloud.resumeai.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * NEW. Adding spring-boot-starter-security would otherwise lock down every
 * endpoint by default, so this explicitly keeps the existing resume/cover
 * letter/export/health endpoints exactly as open as they were before, and
 * only requires a login for the new /api/user/** profile routes.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> {}) // reuses the existing CorsConfig (WebMvcConfigurer) bean
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // let CORS preflight requests through regardless of the rules below
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // existing, pre-existing behaviour: fully open
                    .requestMatchers("/api/health", "/api/resume/**", "/api/cover-letter/**", "/api/export/**").permitAll()
                    // new: open (registration/login must be reachable without a token)
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/uploads/**").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    // new: requires a valid JWT
                    .requestMatchers("/api/user/**").authenticated()
                    .anyRequest().permitAll())
            .headers(headers -> headers.frameOptions(frame -> frame.disable())) // for h2-console
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
