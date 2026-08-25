package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserController: @RequestMapping("/api/admin/users") + @PreAuthorize("hasRole('ADMIN')") ở mức class.
 * Endpoint quản lý user (tạo/sửa/xoá/khoá hàng loạt) là nhạy cảm nhất trong hệ thống,
 * nên test phủ đủ mọi role khác ADMIN đều phải bị chặn 403, và anonymous phải bị chặn 401.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private void assertAuthorized(ResultActions result) throws Exception {
        int status = result.andReturn().getResponse().getStatus();
        assertNotEquals(401, status);
        assertNotEquals(403, status);
    }

    // ---------- GET all / GET by id ----------

    @Test
    void getAllUsers_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void getAllUsers_asSpectator_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getAllUsers_asAdmin_shouldPassAuthorization() throws Exception {
        assertAuthorized(mockMvc.perform(get("/api/admin/users")));
    }

    @Test
    void getUserById_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/users/{id}", "user-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "owner@example.com", roles = "HORSE_OWNER")
    void getUserById_asHorseOwner_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/users/{id}", "user-1"))
                .andExpect(status().isForbidden());
    }

    // ---------- POST create ----------

    @Test
    void createUser_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "jockey@example.com", roles = "JOCKEY")
    void createUser_asJockey_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createUser_asAdmin_shouldPassAuthorization() throws Exception {
        assertAuthorized(mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")));
    }

    // ---------- PUT update ----------

    @Test
    void updateUser_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "referee@example.com", roles = "RACE_REFEREE")
    void updateUser_asReferee_shouldReturn403() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ---------- DELETE ----------

    @Test
    void deleteUser_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/admin/users/{id}", "user-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void deleteUser_asSpectator_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/admin/users/{id}", "user-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void deleteUser_asAdmin_shouldPassAuthorization() throws Exception {
        assertAuthorized(mockMvc.perform(delete("/api/admin/users/{id}", "user-1")));
    }

    // ---------- PUT bulk-status ----------

    @Test
    void updateUsersStatus_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(put("/api/admin/users/bulk-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[\"user-1\"],\"status\":false}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "owner@example.com", roles = "HORSE_OWNER")
    void updateUsersStatus_asHorseOwner_shouldReturn403() throws Exception {
        mockMvc.perform(put("/api/admin/users/bulk-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[\"user-1\"],\"status\":false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void updateUsersStatus_asAdmin_shouldPassAuthorization() throws Exception {
        assertAuthorized(mockMvc.perform(put("/api/admin/users/bulk-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\":[\"user-1\"],\"status\":false}")));
    }
}
