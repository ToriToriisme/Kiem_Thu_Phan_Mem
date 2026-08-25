package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.repository.HorseRepository;
import com.example.horse_racing_management.repository.RaceRepository;
import com.example.horse_racing_management.repository.RegistrationRepository;
import com.example.horse_racing_management.repository.TournamentRepository;
import com.example.horse_racing_management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AdminApprovalController có @PreAuthorize("hasRole('ADMIN')") ở mức CLASS,
 * và endpoint không nằm trong danh sách permitAll của SecurityConfig
 * (chỉ có GET /api/admin/races/** và /api/admin/tournaments/** là public).
 * => Kỳ vọng: anonymous -> 401, authenticated nhưng không phải ADMIN -> 403, ADMIN -> đi qua được.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminApprovalControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationRepository registrationRepository;
    @MockitoBean
    private RaceRepository raceRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private HorseRepository horseRepository;
    @MockitoBean
    private TournamentRepository tournamentRepository;

    private void assertAuthorized(ResultActions result) throws Exception {
        int status = result.andReturn().getResponse().getStatus();
        assertNotEquals(401, status, "Không nên bị chặn ở tầng authentication");
        assertNotEquals(403, status, "Không nên bị chặn ở tầng authorization (ADMIN phải qua được)");
    }

    // ---------- GET /registrations/pending ----------

    @Test
    void getPendingRegistrations_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/management/registrations/pending"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "owner@example.com", roles = "HORSE_OWNER")
    void getPendingRegistrations_asHorseOwner_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/management/registrations/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "jockey@example.com", roles = "JOCKEY")
    void getPendingRegistrations_asJockey_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/management/registrations/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "referee@example.com", roles = "RACE_REFEREE")
    void getPendingRegistrations_asReferee_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/management/registrations/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void getPendingRegistrations_asSpectator_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/management/registrations/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getPendingRegistrations_asAdmin_shouldPassAuthorization() throws Exception {
        assertAuthorized(mockMvc.perform(get("/api/admin/management/registrations/pending")));
    }

    // ---------- PUT .../approve & .../reject ----------

    @Test
    void approveRegistration_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(put("/api/admin/management/registrations/{id}/approve", "reg-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "owner@example.com", roles = "HORSE_OWNER")
    void approveRegistration_asHorseOwner_shouldReturn403() throws Exception {
        mockMvc.perform(put("/api/admin/management/registrations/{id}/approve", "reg-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void approveRegistration_asAdmin_shouldPassAuthorization() throws Exception {
        assertAuthorized(mockMvc.perform(put("/api/admin/management/registrations/{id}/approve", "reg-1")));
    }

    @Test
    void rejectRegistration_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(put("/api/admin/management/registrations/{id}/reject", "reg-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "jockey@example.com", roles = "JOCKEY")
    void rejectRegistration_asJockey_shouldReturn403() throws Exception {
        mockMvc.perform(put("/api/admin/management/registrations/{id}/reject", "reg-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void rejectRegistration_asAdmin_shouldPassAuthorization() throws Exception {
        assertAuthorized(mockMvc.perform(put("/api/admin/management/registrations/{id}/reject", "reg-1")));
    }

    // ---------- GET /referees ----------

    @Test
    void getAllReferees_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/management/referees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void getAllReferees_asSpectator_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/management/referees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getAllReferees_asAdmin_shouldPassAuthorization() throws Exception {
        assertAuthorized(mockMvc.perform(get("/api/admin/management/referees")));
    }

    // ---------- PUT /races/{raceId}/assign-referee/{refereeId} ----------

    @Test
    void assignReferee_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(put("/api/admin/management/races/{raceId}/assign-referee/{refereeId}", "race-1", "ref-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "referee@example.com", roles = "RACE_REFEREE")
    void assignReferee_asReferee_shouldReturn403() throws Exception {
        mockMvc.perform(put("/api/admin/management/races/{raceId}/assign-referee/{refereeId}", "race-1", "ref-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void assignReferee_asAdmin_shouldPassAuthorization() throws Exception {
        assertAuthorized(mockMvc.perform(
                put("/api/admin/management/races/{raceId}/assign-referee/{refereeId}", "race-1", "ref-1")));
    }
}
