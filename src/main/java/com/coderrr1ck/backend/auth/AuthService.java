package com.coderrr1ck.backend.auth;

import com.coderrr1ck.backend.cart.Cart;
import com.coderrr1ck.backend.cart.CartRepository;
import com.coderrr1ck.backend.user.User;
import com.coderrr1ck.backend.user.UserRepository;
import com.coderrr1ck.backend.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<Map<String,String>> saveUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmailAndActiveTrue(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException(registerRequest.getEmail());
        }
        User user = userService.createUser(registerRequest.getUsername(),
                    passwordEncoder.encode(registerRequest.getPassword()),
                    registerRequest.getEmail());

        if (user != null) {
            return ResponseEntity
                    .ok(Map.of("message", "User registered successfully"));
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "User registration failed"));
    }

    public AuthResponse getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String roles = authentication
                .getAuthorities()
                .stream()
                .map((a) -> a.getAuthority().substring(5))
                .toList()
                .toString();
        return AuthResponse.builder()
                .user(authentication.getName())
                .role(roles.substring(1, roles.length() - 1))
                .build();
    }
}
