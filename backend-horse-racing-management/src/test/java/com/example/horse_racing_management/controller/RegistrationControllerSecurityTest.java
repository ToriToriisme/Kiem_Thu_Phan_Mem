package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.service.RegistrationService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RegistrationController ("/api/v1/registrations"):
 * - GET /tournament/{id} là permitAll theo SecurityConfig (đúng, để show lịch đua công khai).
 * - Các endpoint còn lại chỉ cần "đã đăng nhập", KHÔNG kiểm tra role lẫn ownership.
 *
 * Đáng chú ý nhất là 2 endpoint sau — không có kiểm tra ai đang gọi có đúng là
 * chủ ngựa / nài ngựa liên quan hay không (IDOR - Insecure Direct Object Reference):
 *   PUT /{registrationId}/approve-by-jockey   -> lẽ ra chỉ chính jockey đó được duyệt
 *   PUT /{registrationId}/reject-by-jockey    -> tương tự
 *   PUT /{registrationId}/assign-jockey/{jockeyId} -> lẽ ra chỉ chủ ngựa/ADMIN được gán
 * Hiện tại: bất kỳ ai đã login (kể cả SPECTATOR) đều gọi được các API này cho registrationId bất kỳ.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    // ---------- GET public theo thiết kế ----------

    @Test
    void getRegistrationsByTournament_anonymous_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/registrations/tournament/{tournamentId}", "tour-1"))
                .andExpect(status().isOk());
    }

    // ---------- Các GET/PUT khác: hiện chỉ cần login, không cần đúng role/ownership ----------

    @Test
    void getJockeysByHorse_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/registrations/horse/{horseId}/jockeys", "horse-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void assignJockey_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(put("/api/v1/registrations/{registrationId}/assign-jockey/{jockeyId}", "reg-1", "jockey-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Disabled("BUG (IDOR): assign-jockey nên chỉ cho phép chủ ngựa của registration đó hoặc ADMIN")
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void assignJockey_asUnrelatedSpectator_shouldReturn403_expectedBehavior() throws Exception {
        mockMvc.perform(put("/api/v1/registrations/{registrationId}/assign-jockey/{jockeyId}", "reg-1", "jockey-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void assignJockey_asUnrelatedSpectator_currentBehavior_isVulnerable() throws Exception {
        // VULNERABLE: khán giả không liên quan vẫn gán được jockey cho registration của người khác.
        mockMvc.perform(put("/api/v1/registrations/{registrationId}/assign-jockey/{jockeyId}", "reg-1", "jockey-1"))
                .andExpect(status().isOk()); // <-- lẽ ra phải là 403
    }

    @Test
    @Disabled("BUG (IDOR): approve-by-jockey nên chỉ cho phép đúng jockey sở hữu registration đó")
    @WithMockUser(username = "other-jockey@example.com", roles = "JOCKEY")
    void approveByJockey_asUnrelatedJockey_shouldReturn403_expectedBehavior() throws Exception {
        mockMvc.perform(put("/api/v1/registrations/{registrationId}/approve-by-jockey", "reg-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "other-jockey@example.com", roles = "JOCKEY")
    void approveByJockey_asUnrelatedJockey_currentBehavior_isVulnerable() throws Exception {
        // VULNERABLE: bất kỳ jockey nào cũng duyệt được lịch trình của registration không phải của mình.
        mockMvc.perform(put("/api/v1/registrations/{registrationId}/approve-by-jockey", "reg-1"))
                .andExpect(status().isOk()); // <-- lẽ ra phải là 403
    }

    @Test
    void getOwnerRegistrations_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/registrations/owner/{ownerId}/requests", "owner-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Disabled("BUG (IDOR): owner/{ownerId}/requests nên chỉ cho phép chính ownerId đó hoặc ADMIN xem")
    @WithMockUser(username = "other-owner@example.com", roles = "HORSE_OWNER")
    void getOwnerRegistrations_asDifferentOwner_shouldReturn403_expectedBehavior() throws Exception {
        mockMvc.perform(get("/api/v1/registrations/owner/{ownerId}/requests", "owner-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "other-owner@example.com", roles = "HORSE_OWNER")
    void getOwnerRegistrations_asDifferentOwner_currentBehavior_isVulnerable() throws Exception {
        // VULNERABLE: một chủ ngựa xem được danh sách đơn đăng ký của chủ ngựa khác.
        mockMvc.perform(get("/api/v1/registrations/owner/{ownerId}/requests", "owner-1"))
                .andExpect(status().isOk()); // <-- lẽ ra phải là 403
    }
}
