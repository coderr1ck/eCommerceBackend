package com.coderrr1ck.backend.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("register")
    public ResponseEntity<Map<String,String>> register(
            @Valid @RequestBody RegisterRequest registerRequest
    ) {
        return authService.saveUser(registerRequest);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getAuthUser(){
        return ResponseEntity.ok(authService.getAuthUser());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response,
                                       @CookieValue(value = "refreshToken") String refreshToken,
                                       @CookieValue(value = "accessToken",required = false) String accessToken,
                                       @CookieValue(value = "oauthToken",required = false) String oauthToken ) {

        // 1. Invalidate refresh token in DB/Redis (MOST IMPORTANT)
//        implement later

        // 2. Clear access token cookie
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setPath("/");
        accessCookie.setHttpOnly(true);
        accessCookie.setMaxAge(0);

        Cookie oauthCookie = new Cookie("oauthToken", null);
        oauthCookie.setPath("/");
        oauthCookie.setHttpOnly(true);
        oauthCookie.setMaxAge(0);

        // 3. Clear refresh token cookie
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setPath("/api/v1/auth");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(0);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
        response.addCookie(oauthCookie);

        return ResponseEntity.ok().build();
    }

}
