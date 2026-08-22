package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.entity.Horse;
import com.example.horse_racing_management.repository.HorseRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HorseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HorseRepository horseRepository;

    @Test
    void getAllHorses_shouldReturn200AndHorseList() throws Exception {
        Horse horse = new Horse("horse-1", "Thunderbolt", 4, "Thoroughbred", "owner-1");
        when(horseRepository.findAll()).thenReturn(List.of(horse));

        mockMvc.perform(get("/api/v1/horses"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("horse-1"))
                .andExpect(jsonPath("$[0].name").value("Thunderbolt"))
                .andExpect(jsonPath("$[0].age").value(4))
                .andExpect(jsonPath("$[0].breed").value("Thoroughbred"))
                .andExpect(jsonPath("$[0].ownerId").value("owner-1"));
    }

    @Test
    void getHorsesByOwner_shouldReturnOnlyOwnersHorses() throws Exception {
        Horse horse = new Horse("horse-2", "Lightning", 3, "Arabian", "owner-2");
        when(horseRepository.findByOwnerId("owner-2")).thenReturn(List.of(horse));

        mockMvc.perform(get("/api/v1/horses/owner/{ownerId}", "owner-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerId").value("owner-2"));

        verify(horseRepository).findByOwnerId("owner-2");
    }

    @Test
    void createHorse_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/horses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Thunderbolt",
                                  "age": 4,
                                  "breed": "Thoroughbred",
                                  "ownerId": "owner-1"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "owner@example.com", roles = "HORSE_OWNER")
    void createHorse_withAuthentication_shouldReturnSavedHorse() throws Exception {
        Horse saved = new Horse("horse-3", "Thunderbolt", 4, "Thoroughbred", "owner-1");
        when(horseRepository.save(any(Horse.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/horses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Thunderbolt",
                                  "age": 4,
                                  "breed": "Thoroughbred",
                                  "ownerId": "owner-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("horse-3"))
                .andExpect(jsonPath("$.name").value("Thunderbolt"));
    }
}
