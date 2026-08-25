package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.dto.TournamentDTO;
import com.example.horse_racing_management.entity.enums.TournamentStatus;
import com.example.horse_racing_management.service.TournamentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TournamentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TournamentService tournamentService;

    @Test
    void getAllTournaments_shouldReturn200WithoutAuthentication() throws Exception {
        TournamentDTO tournament = new TournamentDTO(
                "tournament-1",
                "Grand Prix 2026",
                "Annual tournament",
                new Date(1785571200000L),
                new Date(1786384800000L),
                TournamentStatus.UPCOMING
        );
        when(tournamentService.getAllTournaments()).thenReturn(List.of(tournament));

        mockMvc.perform(get("/api/admin/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("tournament-1"))
                .andExpect(jsonPath("$[0].name").value("Grand Prix 2026"))
                .andExpect(jsonPath("$[0].status").value("UPCOMING"));
    }

    @Test
    void createTournament_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/admin/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tournamentRequestJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createTournament_withAuthentication_shouldReturnCreatedTournament() throws Exception {
        TournamentDTO created = new TournamentDTO(
                "tournament-2", "Grand Prix 2026", "Annual tournament",
                new Date(1785571200000L), new Date(1786384800000L), TournamentStatus.UPCOMING
        );
        when(tournamentService.createTournament(any(TournamentDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/admin/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tournamentRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("tournament-2"))
                .andExpect(jsonPath("$.name").value("Grand Prix 2026"))
                .andExpect(jsonPath("$.status").value("UPCOMING"));
    }

    private String tournamentRequestJson() {
        return """
                {
                  "name": "Grand Prix 2026",
                  "description": "Annual tournament",
                  "startDate": "2026-08-01T08:00:00.000Z",
                  "endDate": "2026-08-10T18:00:00.000Z",
                  "status": "UPCOMING"
                }
                """;
    }
}
