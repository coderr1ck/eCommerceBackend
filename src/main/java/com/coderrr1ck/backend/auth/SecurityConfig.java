package com.coderrr1ck.backend.auth;

import com.coderrr1ck.backend.filters.JwtAuthProvider;
import com.coderrr1ck.backend.filters.JwtAuthValidationFilter;
import com.coderrr1ck.backend.filters.JwtAuthenticationFilter;
import com.coderrr1ck.backend.filters.JwtRefreshFilter;
import com.coderrr1ck.backend.user.UserService;
import com.coderrr1ck.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    @Value("${ALLOWED_ORIGINS:http://localhost:5173,http://localhost:8080,http://127.0.0.1:5501}")
    private String allowedOrigins;

    public SecurityConfig(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Bean
    public PasswordEncoder setPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider myAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        authenticationProvider.setPasswordEncoder(setPasswordEncoder());
        return authenticationProvider;
    }

    @Bean
    public JwtAuthProvider jwtAuthProvider() {
        return new JwtAuthProvider(userService,jwtUtil);
    }

//    we need to configure manager as well as provider
    @Bean
    public AuthenticationManager myAuthenticationManager(){
        return new ProviderManager(List.of(
                myAuthenticationProvider(),
                jwtAuthProvider()));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        config.setAllowedOrigins(origins);
        config.setAllowedHeaders(Arrays.asList(
                HttpHeaders.ORIGIN,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                HttpHeaders.AUTHORIZATION
        ));
        config.setAllowedMethods(List.of("GET","POST","DELETE","PUT","PATCH","OPTIONS"));
        config.setExposedHeaders(List.of(
                "Authorization"
                        ));
        source.registerCorsConfiguration("/**",config);
        return source;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, JwtAuthValidationFilter jwtAuthValidationFilter, JwtRefreshFilter jwtRefreshFilter,GoogleOAuth2SuccessHandler oAuth2SuccessHandler) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/oauth2/authorization/google",
                                "/api/v1/payments/webhook/razorpay/verify",
                                "/v2/api-docs",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/swagger-ui.html",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                ).sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtRefreshFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(jwtAuthValidationFilter, JwtRefreshFilter.class)
                .build();
    }

}
