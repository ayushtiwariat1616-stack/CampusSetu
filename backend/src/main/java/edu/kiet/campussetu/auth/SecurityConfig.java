package edu.kiet.campussetu.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS with credentials support
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Disable CSRF for REST APIs proxy-handled with SameSite cookies
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(3)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public Information and Health Endpoints
                        .requestMatchers(
                                "/api/health",
                                "/actuator/health",
                                "/api/organization/**",
                                "/api/incidents",
                                "/api/auth/**",
                                "/",
                                "/index.html",
                                "/assets/**",
                                "/favicon.ico",
                                "/favicon.svg",
                                "/robots.txt",
                                "/sitemap.xml",
                                "/static/**",
                                "/error"
                        ).permitAll()
                        // Operator endpoints require authenticated operator session
                        .requestMatchers("/api/operator/**").permitAll() // Checked programmatically via session filter
                        .requestMatchers("/api/admin/**").permitAll() // Checked programmatically via session filter
                        // All other APIs require authenticated session
                        .requestMatchers("/api/**").permitAll() // Handled via SessionUser filter & controller checks
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
