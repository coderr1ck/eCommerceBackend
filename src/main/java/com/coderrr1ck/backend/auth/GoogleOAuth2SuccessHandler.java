package com.coderrr1ck.backend.auth;

import com.coderrr1ck.backend.role.Role;
import com.coderrr1ck.backend.role.RoleRepository;
import com.coderrr1ck.backend.user.*;
import com.coderrr1ck.backend.utils.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Value("${FRONTEND_URL:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
            if (!authentication.isAuthenticated() || authToken.getPrincipal() == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            String email = authToken.getPrincipal().getAttribute("email");
            String username = authToken.getPrincipal().getAttribute("name");

            Optional<User> user = userRepository.findByEmailAndActiveTrue(email);
            if (user.isPresent()) {
                User savedUser = user.get();
                if (!savedUser.getAuthProviders().contains(AuthProvider.GOOGLE)) {
                    savedUser.getAuthProviders().add(AuthProvider.GOOGLE);
                    userRepository.save(savedUser);
                }
            } else {
                Role defaultRole = roleRepository.findByRoleNameAndActiveTrue("ROLE_USER").
                        orElseThrow(() -> new RuntimeException("Default role USER not found"));

                User newUser = User.builder()
                        .email(email)
                        .username(username)
                        .password(null)
                        .roles(Set.of(defaultRole))
                        .authProviders(List.of(AuthProvider.GOOGLE))
                        .active(true)
                        .build();
                userRepository.save(newUser);
            }

            String oauthToken = jwtUtil.generateAccessToken(email);
            Cookie oauthCookie = new Cookie("oauthToken", oauthToken);
            oauthCookie.setHttpOnly(true);
            oauthCookie.setMaxAge(15*60);
            oauthCookie.setPath("/");
            response.addCookie(oauthCookie);

            String refreshToken = jwtUtil.generateRefreshToken(email);
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);
//                refreshCookie.setSecure(true); if only want it to be sent over https
            refreshCookie.setPath("/api/v1/auth");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            response.addCookie(refreshCookie);
            response.sendRedirect(frontendUrl);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
