package com.coderrr1ck.backend.filters;

import com.coderrr1ck.backend.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Service
@Slf4j
public class JwtRefreshFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public JwtRefreshFilter (
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!request.getServletPath().equals("/api/v1/auth/refresh")){
            log.info("Refresh Filter Intercepted Request -> But not a refresh request passing forward");
            filterChain.doFilter(request,response);
            return;
        }
        // Implement refresh token logic here
        String refreshToken = getTokenFromRequestCookie(request);
        if(refreshToken == null){
            log.info("Refresh Filter Intercepted Request -> Refresh token is missing returning response 401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        JwtAuthenticationToken authToken = new JwtAuthenticationToken(refreshToken);
        Authentication authResult = authenticationManager.authenticate(authToken);
        if(authResult.isAuthenticated()){
            String newAccessToken = jwtUtil.generateAccessToken(authResult.getName());
            response.setHeader("Authorization", "Bearer " + newAccessToken);
//            no need to implment new refresh token generation for now
        }

    }

    private String getTokenFromRequestCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) return null;
        String refreshToken = null;
        for(Cookie cookie : cookies){
            if(cookie.getName().equals("refreshToken")){
                refreshToken = cookie.getValue();
                break;
            }
        }
        return refreshToken;
    }
}
