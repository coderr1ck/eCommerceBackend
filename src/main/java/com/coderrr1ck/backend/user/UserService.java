package com.coderrr1ck.backend.user;

import com.coderrr1ck.backend.auth.RegisterRequest;
import com.coderrr1ck.backend.role.Role;
import com.coderrr1ck.backend.role.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmailAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }


    public User createUser(String username,String encodedPassword,String email) {
        Role defaultRole = roleRepository.findByRoleNameAndActiveTrue("ROLE_USER").
                orElseThrow(() -> new RuntimeException("Default role USER not found"));

        User user = User.builder()
                .username(username)
                .email(email)
                .password(encodedPassword)
                .roles(Set.of(defaultRole))
                .authProviders(List.of(AuthProvider.LOCAL))
                .active(true)
                .build();

        return userRepository.save(user);
    }
}
