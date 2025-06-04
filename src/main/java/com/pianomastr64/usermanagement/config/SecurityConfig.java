package com.pianomastr64.usermanagement.config;

import com.pianomastr64.usermanagement.user.UserRepository;
import com.pianomastr64.usermanagement.security.JwtAuthFilter;
import com.pianomastr64.usermanagement.security.JwtUtil;
import com.pianomastr64.usermanagement.util.ErrorResponseUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    
    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository repo) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/register", "/h2-console/**").permitAll()
                // TODO add /admin/ endpoints in the future
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/users/**").authenticated()
                .anyRequest().denyAll()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) ->
                    ErrorResponseUtil.writeJsonError(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized. Please log in."))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    ErrorResponseUtil.writeJsonError(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access denied. You do not have permission to access this resource."))
            )
            // to allow H2 console to work
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(new JwtAuthFilter(jwtUtil, repo), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
