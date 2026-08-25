package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.dto.JockeyDTO;
import com.example.horse_racing_management.service.JockeyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JockeyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JockeyService jockeyService;

    @Test
    void getAllJockeys_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/jockeys"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void getAllJockeys_withAuthentication_shouldReturn200AndList() throws Exception {
        JockeyDTO jockey = new JockeyDTO("jockey-1", "John Doe", "JC-001", 5, 4.8, "user-1");
        when(jockeyService.getAllJockeys()).thenReturn(List.of(jockey));

        mockMvc.perform(get("/api/v1/jockeys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("jockey-1"))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].licenseNumber").value("JC-001"));
    }

    @Test
    @WithMockUser(username = "spectator@example.com", roles = "SPECTATOR")
    void getJockeyById_shouldReturnRequestedJockey() throws Exception {
        JockeyDTO jockey = new JockeyDTO("jockey-2", "Jane Doe", "JC-002", 3, 4.5, "user-2");
        when(jockeyService.getJockeyById("jockey-2")).thenReturn(jockey);

        mockMvc.perform(get("/api/v1/jockeys/{id}", "jockey-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("jockey-2"))
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void createJockey_shouldReturn201AndCreatedJockey() throws Exception {
        JockeyDTO created = new JockeyDTO("jockey-3", "New Jockey", "JC-003", 2, 4.0, "user-3");
        when(jockeyService.createJockey(any(JockeyDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/jockeys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "New Jockey",
                                  "licenseNumber": "JC-003",
                                  "experienceYears": 2,
                                  "rating": 4.0,
                                  "userId": "user-3"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("jockey-3"))
                .andExpect(jsonPath("$.licenseNumber").value("JC-003"));
    }
}
