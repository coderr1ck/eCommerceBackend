package com.coderrr1ck.backend.auth;

import com.coderrr1ck.backend.filters.JwtAuthProvider;
import com.coderrr1ck.backend.filters.JwtAuthValidationFilter;
import com.coderrr1ck.backend.filters.JwtAuthenticationFilter;
import com.coderrr1ck.backend.filters.JwtRefreshFilter;
import com.coderrr1ck.backend.user.UserService;
import com.coderrr1ck.backend.utils.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
public class SecurityConfig {

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, JwtAuthValidationFilter jwtAuthValidationFilter, JwtRefreshFilter jwtRefreshFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                ).sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtRefreshFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(jwtAuthValidationFilter, JwtRefreshFilter.class)
                .build();
    }

}
