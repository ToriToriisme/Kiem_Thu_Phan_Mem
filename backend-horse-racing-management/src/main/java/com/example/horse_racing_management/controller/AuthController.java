package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.dto.AuthResponse;
import com.example.horse_racing_management.dto.LoginRequest;
import com.example.horse_racing_management.dto.RegisterRequest;
import com.example.horse_racing_management.entity.Permission;
import com.example.horse_racing_management.entity.Role;
import com.example.horse_racing_management.entity.User;
import com.example.horse_racing_management.repository.RoleRepository;
import com.example.horse_racing_management.repository.UserRepository;
import com.example.horse_racing_management.repository.JockeyRepository;
import com.example.horse_racing_management.entity.Jockey;
import com.example.horse_racing_management.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JockeyRepository jockeyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateJwtToken(authentication);

            // Fetch User to populate AuthResponse
            User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
            List<String> permissions = user.getRole() != null && user.getRole().getPermissions() != null
                    ? user.getRole().getPermissions().stream().map(Permission::getKey).collect(Collectors.toList())
                    : List.of();

            AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getBalance(),
                    user.getRole() != null ? user.getRole().getKey() : null,
                    permissions
            );

            return ResponseEntity.ok(new AuthResponse(jwt, userInfo));
        } catch (org.springframework.security.authentication.DisabledException e) {
            return ResponseEntity.status(401).body(Map.of("message", "Tài khoản bị khóa"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Sai email hoặc mật khẩu"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Email is already in use!"));
        }

        // Chặn tạo Admin từ màn hình đăng ký ngoài
        if ("ROLE_ADMIN".equals(registerRequest.getRoleKey())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Cannot register as ADMIN."));
        }

        Role role = roleRepository.findByKey(registerRequest.getRoleKey())
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setRole(role);

        // System.err.println(user);
        userRepository.save(user);

        // Auto-create Jockey profile if role is ROLE_JOCKEY
        if ("ROLE_JOCKEY".equals(registerRequest.getRoleKey())) {
            Jockey jockey = new Jockey();
            jockey.setUserId(user.getId());
            jockey.setName(user.getFullName());
            jockey.setLicenseNumber("JC-" + System.currentTimeMillis());
            jockey.setExperienceYears(0);
            jockey.setRating(0.0);
            jockeyRepository.save(jockey);
        }

        return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        List<String> permissions = user.getRole() != null && user.getRole().getPermissions() != null
                ? user.getRole().getPermissions().stream().map(Permission::getKey).collect(Collectors.toList())
                : List.of();

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getBalance(),
                user.getRole() != null ? user.getRole().getKey() : null,
                permissions
        );

        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        
        // If email is being changed, check if it's already in use
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email đã được sử dụng!"));
            }
            user.setEmail(request.getEmail());
        }

        // Change password is now handled in a separate endpoint
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Cập nhật hồ sơ thành công!"));
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest request) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu cũ không chính xác!"));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công!"));
    }

    public static class UpdateProfileRequest {
        private String fullName;
        private String email;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
