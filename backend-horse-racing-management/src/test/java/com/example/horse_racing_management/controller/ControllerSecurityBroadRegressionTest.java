package com.example.horse_racing_management.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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

import com.example.horse_racing_management.dto.JockeyDTO;
import com.example.horse_racing_management.dto.PermissionDTO;
import com.example.horse_racing_management.service.JockeyService;
import com.example.horse_racing_management.service.PermissionService;

@SpringBootTest
/**
 * Regression test mở rộng cho nhiều controller liên quan phân quyền.
 * Chạy: ./mvnw -Dtest=ControllerSecurityBroadRegressionTest test
 */
class ControllerSecurityBroadRegressionTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private JockeyService jockeyService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        Mockito.reset(permissionService, jockeyService);
        when(permissionService.getPermissionById("1")).thenReturn(new PermissionDTO("1", "Read", "READ"));
        when(jockeyService.getJockeyById("1")).thenReturn(new JockeyDTO("1", "Alice", "LIC-1", 5, 4.5, "user-1"));

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        PermissionService permissionService() {
            return Mockito.mock(PermissionService.class);
        }

        @Bean
        JockeyService jockeyService() {
            return Mockito.mock(JockeyService.class);
        }
    }

    @Test
    void shouldRequireAuthenticationForPermissionEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/permissions/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldAllowAuthenticatedUserForPermissionEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/permissions/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForRaceManagementEndpoint() throws Exception {
        mockMvc.perform(delete("/api/admin/races/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldAllowAuthenticatedUserForRaceManagementEndpoint() throws Exception {
        mockMvc.perform(put("/api/admin/races/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldRequireAuthenticationForTournamentManagementEndpoint() throws Exception {
        mockMvc.perform(post("/api/admin/tournaments/1/register"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldAllowAuthenticatedUserForTournamentManagementEndpoint() throws Exception {
        mockMvc.perform(post("/api/admin/tournaments/1/register"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldRequireAuthenticationForJockeyEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/jockeys/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldAllowAuthenticatedUserForJockeyEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/jockeys/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForRegistrationEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/registrations/horse/horse-1/jockeys"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldAllowAuthenticatedUserForRegistrationEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/registrations/horse/horse-1/jockeys"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForRewardEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/calculate/race-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldAllowAuthenticatedUserForRewardEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/rewards/calculate/race-1"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldRequireAuthenticationForSpectatorEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/spectator/bets/history/spectator-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldAllowAuthenticatedUserForSpectatorEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/spectator/bets/history/spectator-1"))
                .andExpect(status().isOk());
    }
}
