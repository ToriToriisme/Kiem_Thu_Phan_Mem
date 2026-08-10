package com.example.horse_racing_management.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
/**
 * Regression test cho user/role management permission.
 * Chạy: ./mvnw -Dtest=UserRoleSecurityRegressionTest test
 */
class UserRoleSecurityRegressionTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void shouldRequireAuthenticationForAdminUsersEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldRejectNonAdminForAdminUsersEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthenticationForAdminRolesEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldRejectNonAdminForAdminRolesEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldRejectNonAdminForBulkStatusUpdate() throws Exception {
        mockMvc.perform(put("/api/admin/users/bulk-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[\"1\"],\"status\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldRejectNonAdminForDeleteRole() throws Exception {
        mockMvc.perform(delete("/api/admin/roles/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldRejectNonAdminForCreateRole() throws Exception {
        mockMvc.perform(post("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"ROLE_TEST\",\"name\":\"Test\"}"))
                .andExpect(status().isForbidden());
    }
}
