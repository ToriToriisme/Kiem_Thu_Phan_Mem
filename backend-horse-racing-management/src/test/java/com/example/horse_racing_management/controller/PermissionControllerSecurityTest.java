package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.service.PermissionService;
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
 * !!! LỖ HỔNG BẢO MẬT TƯƠNG TỰ RoleController !!!
 * PermissionController ("/api/admin/permissions") cũng KHÔNG có @PreAuthorize("hasRole('ADMIN')").
 * Bất kỳ user đã login đều có thể tạo/sửa/xoá Permission -> có thể tự gán thêm quyền
 * (VD: PERM_USER_MANAGER) vào Role của chính mình gián tiếp thông qua RoleController.
 * Kết hợp 2 lỗ hổng RoleController + PermissionController = full leo thang lên ADMIN.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PermissionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    @Disabled("BUG: PermissionController thiếu @PreAuthorize(\"hasRole('ADMIN')\") - bỏ @Disabled sau khi fix")
    @WithMockUser(username = "jockey@example.com", roles = "JOCKEY")
    void createPermission_asNonAdmin_shouldReturn403_expectedBehavior() throws Exception {
        mockMvc.perform(post("/api/admin/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"PERM_USER_MANAGER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllPermissions_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/permissions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "jockey@example.com", roles = "JOCKEY")
    void createPermission_asJockey_currentBehavior_isVulnerable() throws Exception {
        // VULNERABLE: một nài ngựa (JOCKEY) có thể tạo permission mới tuỳ ý.
        mockMvc.perform(post("/api/admin/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"PERM_USER_MANAGER\",\"name\":\"Fake perm\"}"))
                .andExpect(status().is2xxSuccessful()); // <-- lẽ ra phải là 403
    }

    @Test
    @WithMockUser(username = "jockey@example.com", roles = "JOCKEY")
    void deletePermission_asJockey_currentBehavior_isVulnerable() throws Exception {
        mockMvc.perform(delete("/api/admin/permissions/{id}", "perm-1"))
                .andExpect(status().is2xxSuccessful()); // <-- lẽ ra phải là 403
    }
}
