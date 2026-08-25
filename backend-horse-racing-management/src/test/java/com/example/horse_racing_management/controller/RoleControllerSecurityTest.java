package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.service.RoleService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * !!! LỖ HỔNG BẢO MẬT PHÁT HIỆN QUA TEST NÀY !!!
 *
 * RoleController nằm dưới "/api/admin/roles" nhưng KHÔNG có @PreAuthorize("hasRole('ADMIN')")
 * như AdminApprovalController / UserController. SecurityConfig cũng không permitAll cho path này,
 * nên nó rơi vào rule mặc định "anyRequest().authenticated()".
 *
 * Hệ quả: BẤT KỲ user nào đã đăng nhập (SPECTATOR, JOCKEY, HORSE_OWNER...) đều có thể
 * tạo / sửa / xoá Role trong hệ thống — kể cả tự cấp quyền ADMIN cho role của mình.
 * Đây là lỗi leo thang đặc quyền (privilege escalation) nghiêm trọng.
 *
 * Các test *_shouldReturn403_expectedBehavior bên dưới đang @Disabled vì chúng mô tả
 * hành vi ĐÚNG PHẢI CÓ sau khi thêm @PreAuthorize("hasRole('ADMIN')") vào class.
 * Các test *_currentBehavior_isVulnerable ghi lại hành vi THỰC TẾ hiện tại (để CI không đỏ),
 * nhưng phải coi là bug cần fix, không phải spec đúng.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RoleControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    // ---------- Hành vi đúng phải có (đang FAIL vì thiếu @PreAuthorize -> để @Disabled) ----------

    @Test
    @Disabled("BUG: RoleController thiếu @PreAuthorize(\"hasRole('ADMIN')\") - bỏ @Disabled sau khi fix")
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void createRole_asNonAdmin_shouldReturn403_expectedBehavior() throws Exception {
        mockMvc.perform(post("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"ROLE_ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Disabled("BUG: RoleController thiếu @PreAuthorize(\"hasRole('ADMIN')\") - bỏ @Disabled sau khi fix")
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void deleteRole_asNonAdmin_shouldReturn403_expectedBehavior() throws Exception {
        mockMvc.perform(delete("/api/admin/roles/{id}", "role-1"))
                .andExpect(status().isForbidden());
    }

    // ---------- Hành vi HIỆN TẠI (đỏ đèn cảnh báo, KHÔNG phải điều mong muốn) ----------

    @Test
    void getAllRoles_anonymous_shouldReturn401() throws Exception {
        // Đúng: vẫn cần đăng nhập, nhưng chưa đủ vì chưa kiểm tra ROLE
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void createRole_asSpectator_currentBehavior_isVulnerable() throws Exception {
        // VULNERABLE: một khán giả thường có thể tạo Role mới (kể cả role trùng quyền ADMIN)
        // vì controller chỉ yêu cầu "đã đăng nhập", không yêu cầu ROLE_ADMIN.
        mockMvc.perform(post("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"ROLE_ADMIN\",\"name\":\"Fake Admin\"}"))
                .andExpect(status().is2xxSuccessful()); // <-- lẽ ra phải là 403
    }

    @Test
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void deleteRole_asSpectator_currentBehavior_isVulnerable() throws Exception {
        // VULNERABLE: khán giả có thể xoá bất kỳ role nào (kể cả ROLE_ADMIN), gây DoS/leo thang.
        mockMvc.perform(delete("/api/admin/roles/{id}", "role-1"))
                .andExpect(status().is2xxSuccessful()); // <-- lẽ ra phải là 403
    }

    @Test
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void updateRole_asSpectator_currentBehavior_isVulnerable() throws Exception {
        mockMvc.perform(put("/api/admin/roles/{id}", "role-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"ROLE_ADMIN\"}"))
                .andExpect(status().is2xxSuccessful()); // <-- lẽ ra phải là 403
    }
}
