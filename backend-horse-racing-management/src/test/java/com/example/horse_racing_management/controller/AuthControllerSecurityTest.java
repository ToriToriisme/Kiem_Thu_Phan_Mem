package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.entity.Role;
import com.example.horse_racing_management.entity.User;
import com.example.horse_racing_management.repository.JockeyRepository;
import com.example.horse_racing_management.repository.RoleRepository;
import com.example.horse_racing_management.repository.UserRepository;
import com.example.horse_racing_management.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController: toàn bộ "/api/auth/**" là permitAll trong SecurityConfig (đúng chủ đích cho
 * login/register). Riêng /me, /me (PUT), /me/password KHÔNG được Spring Security tự chặn ở tầng
 * filter (vì path permitAll) — controller tự kiểm tra "authentication == null ||
 * !authentication.isAuthenticated() || anonymousUser" và trả 401 bằng tay.
 *
 * Test này đảm bảo lớp kiểm tra thủ công đó hoạt động đúng, vì nếu ai đó vô tình xoá đoạn
 * check này trong lúc refactor, KHÔNG có lớp bảo vệ nào khác (SecurityConfig) đứng sau đỡ.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private RoleRepository roleRepository;
    @MockitoBean
    private JockeyRepository jockeyRepository;
    @MockitoBean
    private JwtTokenProvider tokenProvider;

    // ---------- login / register phải luôn public ----------

    @Test
    void login_anonymous_shouldNotBeBlockedByFilterChain() throws Exception {
        when(authenticationManager.authenticate(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new BadCredentialsException("bad creds"));

        // Không nên là 401 do bị chặn filter (permitAll) - nhưng controller vẫn có thể trả 401
        // do sai mật khẩu; điều ta thật sự muốn chắc chắn là request CHẠM ĐƯỢC tới controller.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Sai email hoặc mật khẩu"));
    }

    @Test
    void register_anonymous_shouldReachControllerLogic() throws Exception {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        // Mockito.mock(Role.class) né được việc phải biết constructor/setter thật của Role -
        // chỉ cần roleRepository trả về MỘT Role bất kỳ để orElseThrow() không ném exception,
        // giúp request đi trọn tới cuối controller thay vì bị dừng giữa chừng bởi lỗi nghiệp vụ.
        when(roleRepository.findByKey("ROLE_SPECTATOR")).thenReturn(Optional.of(mock(Role.class)));

        // Mục tiêu của test KHÔNG phải kiểm tra business logic đăng ký thành công hay không,
        // mà là chứng minh request KHÔNG bị chặn ở tầng 401/403 -> endpoint thật sự permitAll.
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "newuser",
                                  "email": "new@example.com",
                                  "password": "secret123",
                                  "fullName": "New User",
                                  "roleKey": "ROLE_SPECTATOR"
                                }
                                """))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertNotEquals(401, status);
                    org.junit.jupiter.api.Assertions.assertNotEquals(403, status);
                });
    }

    @Test
    void register_withRoleAdmin_shouldBeBlockedByBusinessLogic() throws Exception {
        // Không phải lỗ hổng - controller CHỦ ĐỘNG chặn tự đăng ký làm ADMIN. Test để giữ hành vi này.
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "hacker",
                                  "email": "hacker@example.com",
                                  "password": "secret123",
                                  "fullName": "Hacker",
                                  "roleKey": "ROLE_ADMIN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Cannot register as ADMIN."));
    }

    // ---------- /me: bảo vệ thủ công trong controller ----------

    @Test
    void getCurrentUser_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "SPECTATOR")
    void getCurrentUser_withAuthentication_shouldPassManualCheck() throws Exception {
        // Mockito.mock(User.class) trả về giá trị mặc định an toàn cho mọi getter (null/0/false),
        // đủ để controller chạy hết logic build AuthResponse.UserInfo mà không NPE hay throw.
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mock(User.class)));

        // Quan trọng là KHÔNG còn là 401 "Not authenticated" nữa - tức đã qua được lớp kiểm tra
        // thủ công "authentication == null || anonymousUser" trong AuthController.
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertNotEquals(401, status);
                });
    }

    @Test
    void updateProfile_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(put("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"New Name\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated"));
    }

    @Test
    void changePassword_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(put("/api/auth/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"a\",\"newPassword\":\"b\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated"));
    }
}
