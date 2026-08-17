package com.example.horse_racing_management.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
/**
 * Regression test cho workflow admin approval.
 * Chạy: ./mvnw -Dtest=AdminApprovalSecurityRegressionTest test
 */
class AdminApprovalSecurityRegressionTest {

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
    void shouldRequireAuthenticationForPendingRegistrationsEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/management/registrations/pending"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldRejectNonAdminForPendingRegistrationsEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/management/registrations/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldAllowAdminForPendingRegistrationsEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/management/registrations/pending"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForApproveRegistrationEndpoint() throws Exception {
        mockMvc.perform(put("/api/admin/management/registrations/does-not-matter/approve"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldRejectNonAdminForApproveRegistrationEndpoint() throws Exception {
        mockMvc.perform(put("/api/admin/management/registrations/does-not-matter/approve"))
                .andExpect(status().isForbidden());
    }
}
