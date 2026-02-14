package com.coderrr1ck.backend.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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

}
