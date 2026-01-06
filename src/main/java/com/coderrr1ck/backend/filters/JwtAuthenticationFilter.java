package com.coderrr1ck.backend.filters;

import com.coderrr1ck.backend.auth.LoginRequest;
import com.coderrr1ck.backend.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Service
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationFilter(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {

            if(!request.getServletPath().equals("/api/v1/auth/login")){
                log.info("JWT Auth Filter Intercepted Request -> But not a login request passing forward");
                filterChain.doFilter(request,response);
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            LoginRequest loginRequest = mapper.readValue(request.getInputStream(), LoginRequest.class);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );

            Authentication authentication = authenticationManager.authenticate(authToken);
            if(authentication.isAuthenticated()){
                String accessToken = jwtUtil.generateAccessToken(authentication.getName());
                response.setHeader("Authorization", "Bearer " + accessToken);

                String refreshToken = jwtUtil.generateRefreshToken(authentication.getName());
                Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
                refreshCookie.setHttpOnly(true);
//                refreshCookie.setSecure(true); if only want it to be sent over https
                refreshCookie.setPath("/api/v1/auth/refresh");
                refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
                response.addCookie(refreshCookie);
            }
        }catch (Exception ex){
            log.info("JWT Auth Filter Intercepted Request -> Exception occurred: {}", ex.getMessage());
            resolver.resolveException(request, response, null, ex);
        }



    }
}
