package com.coderrr1ck.backend.auth;

import com.coderrr1ck.backend.user.User;
import com.coderrr1ck.backend.user.UserRepository;
import com.coderrr1ck.backend.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

}
