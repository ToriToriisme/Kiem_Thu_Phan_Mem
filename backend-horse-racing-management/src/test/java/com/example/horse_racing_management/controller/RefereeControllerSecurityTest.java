package com.example.horse_racing_management.controller;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.horse_racing_management.service.RefereeService;

@SpringBootTest
/**
 * Regression test cho endpoint referee.
 * Chạy: ./mvnw -Dtest=RefereeControllerSecurityTest test
 */
class RefereeControllerSecurityTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RefereeController refereeController;

    private MockMvc mockMvc;
    private RefereeService refereeService;

    @BeforeEach
    void setUp() {
        refereeService = Mockito.mock(RefereeService.class);
        ReflectionTestUtils.setField(refereeController, "refereeService", refereeService);

        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void shouldRequireAuthenticationForAssignedRacesEndpoint() throws Exception {
        mockMvc.perform(get("/api/referee/referee-1/assigned-races"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "referee")
    void shouldAllowAuthenticatedUserToAccessAssignedRacesEndpoint() throws Exception {
        when(refereeService.getAssignedRaces("referee-1")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/referee/referee-1/assigned-races"))
                .andExpect(status().isOk());

        verify(refereeService).getAssignedRaces("referee-1");
    }

    @Test
    void shouldRequireAuthenticationForRaceDetailsEndpoint() throws Exception {
        mockMvc.perform(get("/api/referee/race/race-1/details"))
                .andExpect(status().isUnauthorized());
    }
}
