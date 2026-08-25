package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.service.SpectatorService;
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
 * SpectatorController ("/api/v1/spectator"):
 * - GET /races và /races/live là permitAll (đúng, xem đua trực tiếp không cần login).
 * - Các endpoint còn lại liên quan TIỀN (ví, cược) chỉ cần "đã đăng nhập", KHÔNG kiểm tra:
 *     (a) role có phải SPECTATOR không (ADMIN/JOCKEY/REFEREE login vẫn gọi được),
 *     (b) {spectatorId} trong path có đúng là chính người gọi hay không.
 *
 * (b) là lỗ hổng IDOR nghiêm trọng nhất trong toàn bộ hệ thống: bất kỳ user đã login nào
 * cũng có thể xem số dư ví, nạp tiền, hoặc đặt cược thay cho MỘT spectatorId bất kỳ khác,
 * chỉ cần biết/đoán được id đó.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SpectatorControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpectatorService spectatorService;

    // ---------- GET public theo thiết kế ----------

    @Test
    void getLiveRaces_anonymous_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/spectator/races/live"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRaces_anonymous_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/spectator/races"))
                .andExpect(status().isOk());
    }

    // ---------- Endpoint tiền bạc: ít nhất phải chặn anonymous ----------

    @Test
    void getWalletBalance_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/spectator/wallet/{spectatorId}", "spec-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void topUpWallet_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/spectator/wallet/top-up/{spectatorId}", "spec-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void placeBet_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/spectator/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- IDOR: hành vi mong muốn (đang @Disabled vì chưa được kiểm tra trong code) ----------

    @Test
    @Disabled("BUG (IDOR): xem ví của spectator khác phải bị 403 nếu id không khớp principal")
    @WithMockUser(username = "spectator-2@example.com", roles = "SPECTATOR")
    void getWalletBalance_ofDifferentSpectator_shouldReturn403_expectedBehavior() throws Exception {
        mockMvc.perform(get("/api/v1/spectator/wallet/{spectatorId}", "spec-1-not-mine"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Disabled("BUG (IDOR): nạp tiền hộ spectator khác phải bị 403")
    @WithMockUser(username = "spectator-2@example.com", roles = "SPECTATOR")
    void topUpWallet_ofDifferentSpectator_shouldReturn403_expectedBehavior() throws Exception {
        mockMvc.perform(post("/api/v1/spectator/wallet/top-up/{spectatorId}", "spec-1-not-mine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1000000}"))
                .andExpect(status().isForbidden());
    }

    // ---------- Hành vi HIỆN TẠI: minh chứng lỗ hổng ----------

    @Test
    @WithMockUser(username = "spectator-2@example.com", roles = "SPECTATOR")
    void getWalletBalance_ofDifferentSpectator_currentBehavior_isVulnerable() throws Exception {
        // VULNERABLE: đọc được số dư ví của người khác chỉ bằng cách đổi spectatorId trên URL.
        mockMvc.perform(get("/api/v1/spectator/wallet/{spectatorId}", "spec-1-not-mine"))
                .andExpect(status().isOk()); // <-- lẽ ra phải là 403
    }

    @Test
    @WithMockUser(username = "spectator-2@example.com", roles = "SPECTATOR")
    void topUpWallet_ofDifferentSpectator_currentBehavior_isVulnerable() throws Exception {
        // VULNERABLE: nạp tiền vào ví người khác (rủi ro rửa tiền / gian lận số dư).
        mockMvc.perform(post("/api/v1/spectator/wallet/top-up/{spectatorId}", "spec-1-not-mine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1000000}"))
                .andExpect(status().isOk()); // <-- lẽ ra phải là 403
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void placeBet_asAdminRoleInsteadOfSpectator_currentBehavior_isVulnerable() throws Exception {
        // VULNERABLE: ADMIN (không phải role SPECTATOR) vẫn đặt cược được vì controller
        // không giới hạn role, chỉ cần "đã đăng nhập".
        mockMvc.perform(post("/api/v1/spectator/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk()); // ranh giới nghiệp vụ đang không được thực thi
    }

    @Test
    @WithMockUser(username = "spectator-2@example.com", roles = "SPECTATOR")
    void getBetHistory_ofDifferentSpectator_currentBehavior_isVulnerable() throws Exception {
        // VULNERABLE: xem được lịch sử cược (thói quen chi tiêu) của người khác.
        mockMvc.perform(get("/api/v1/spectator/bets/history/{spectatorId}", "spec-1-not-mine"))
                .andExpect(status().isOk()); // <-- lẽ ra phải là 403
    }
}
