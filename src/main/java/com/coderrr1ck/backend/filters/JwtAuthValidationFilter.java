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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Service
@AllArgsConstructor
@Slf4j
public class JwtAuthValidationFilter extends OncePerRequestFilter {

    private static final String[] DOCS_URLS = {
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui",
            "/api/v1/auth/register"
    };
    private final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        for(String url: DOCS_URLS){
            if(request.getServletPath().startsWith(url)){
                log.info("JWT Auth Validation Filter Intercepted Request -> But docs request so passing forward");
                filterChain.doFilter(request,response);
                return;
            }
        }

        if(request.getServletPath().equals("/oauth2/authorization/google") ){
            log.info("JWT Auth Validation Filter Intercepted Request -> But oauth request so passing forward");
            filterChain.doFilter(request,response);
            return;
        }

        log.info("JWT Auth Validation Filter Intercepted Request -> Processing JWT validation");
        String token = getTokenFromRequest(request);
        if(token == null){
            token = getTokenFromRequestCookie(request);
        }
        if(token != null){
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(token);
            Authentication authentication = authenticationManager.authenticate(authToken);
            if(authentication.isAuthenticated()){
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        }else {
            log.info("JWT Auth Validation Filter Intercepted Request -> JWT token is missing returning response 401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private String getTokenFromRequestCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) return null;
        for(Cookie cookie: cookies){
            if(cookie.getName().equals("oauthToken") || cookie.getName().equals("accessToken")){
                return cookie.getValue();
            }
        }
        return null;
    }
}
