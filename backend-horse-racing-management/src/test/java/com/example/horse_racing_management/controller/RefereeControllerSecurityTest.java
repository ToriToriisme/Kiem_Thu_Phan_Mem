package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.service.RefereeService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RefereeControllerSecurityTest.TestConfig.class)
class RefereeControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RefereeService refereeService;

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

    @Configuration
    static class TestConfig {

        @Bean
        RefereeService refereeService() {
            return Mockito.mock(RefereeService.class);
        }

        @Bean
        RefereeController refereeController(RefereeService refereeService) {
            RefereeController controller = new RefereeController();
            ReflectionTestUtils.setField(controller, "refereeService", refereeService);
            return controller;
        }

        @Bean
        MockMvc mockMvc(RefereeController controller) {
            return MockMvcBuilders.standaloneSetup(controller)
                    .addFilters(new AuthenticationRequiredFilter())
                    .build();
        }
    }

    static class AuthenticationRequiredFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            filterChain.doFilter(request, response);
        }
    }
}
