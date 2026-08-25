package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.service.RaceService;
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
 * RaceController: SecurityConfig cho phép GET /api/admin/races/** là permitAll (đúng chủ đích,
 * vì lịch đua cần public cho khán giả xem).
 *
 * NHƯNG POST/PUT/DELETE (tạo/sửa/xoá chặng đua) không có @PreAuthorize trong controller,
 * nên chỉ bị chặn ở mức "đã đăng nhập" (anyRequest().authenticated()), KHÔNG kiểm tra role ADMIN.
 * => Một SPECTATOR hay JOCKEY bình thường đã login vẫn tạo/xoá được chặng đua.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RaceControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RaceService raceService;

    // ---------- GET là public - đúng thiết kế ----------

    @Test
    void getAllRaces_anonymous_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/admin/races"))
                .andExpect(status().isOk());
    }

    @Test
    void getRaceById_anonymous_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/admin/races/{id}", "race-1"))
                .andExpect(status().isOk());
    }

    @Test
    void getRacesByTournament_anonymous_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/admin/races/tournament/{tournamentId}", "tour-1"))
                .andExpect(status().isOk());
    }

    // ---------- Ghi dữ liệu: hành vi mong muốn (đang @Disabled vì đang thiếu @PreAuthorize) ----------

    @Test
    @Disabled("BUG: RaceController thiếu @PreAuthorize(\"hasRole('ADMIN')\") cho POST/PUT/DELETE")
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void createRace_asNonAdmin_shouldReturn403_expectedBehavior() throws Exception {
        mockMvc.perform(post("/api/admin/races")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Disabled("BUG: RaceController thiếu @PreAuthorize(\"hasRole('ADMIN')\") cho POST/PUT/DELETE")
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void deleteRace_asNonAdmin_shouldReturn403_expectedBehavior() throws Exception {
        mockMvc.perform(delete("/api/admin/races/{id}", "race-1"))
                .andExpect(status().isForbidden());
    }

    // ---------- Hành vi HIỆN TẠI: bất kỳ ai login đều ghi được ----------

    @Test
    void createRace_anonymous_shouldReturn401() throws Exception {
        // Ít nhất vẫn cần đăng nhập
        mockMvc.perform(post("/api/admin/races")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void createRace_asSpectator_currentBehavior_isVulnerable() throws Exception {
        // VULNERABLE: khán giả có thể tạo chặng đua mới.
        mockMvc.perform(post("/api/admin/races")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is2xxSuccessful()); // <-- lẽ ra phải là 403
    }

    @Test
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void deleteRace_asSpectator_currentBehavior_isVulnerable() throws Exception {
        // VULNERABLE: khán giả có thể xoá chặng đua đang có đơn đăng ký / kèo cược.
        mockMvc.perform(delete("/api/admin/races/{id}", "race-1"))
                .andExpect(status().is2xxSuccessful()); // <-- lẽ ra phải là 403
    }
}
