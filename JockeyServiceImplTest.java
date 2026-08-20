package com.example.horse_racing_management.service;

import com.example.horse_racing_management.dto.JockeyDTO;
import com.example.horse_racing_management.entity.Jockey;
import com.example.horse_racing_management.repository.JockeyRepository;
import com.example.horse_racing_management.service.impl.JockeyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JockeyServiceImplTest {

    @Mock
    private JockeyRepository jockeyRepository;

    @Mock
    private com.example.horse_racing_management.repository.UserRepository userRepository;

    @InjectMocks
    private JockeyServiceImpl jockeyService;

    @Test
    void createJockey_success() {
        JockeyDTO dto = new JockeyDTO(
                null,
                "Nguyễn Văn A",
                "JC001",
                5,
                8.5,
                "user01");

        Jockey saved = new Jockey(
                "jockey01",
                "Nguyễn Văn A",
                "JC001",
                5,
                8.5,
                "user01");

        when(jockeyRepository.findByLicenseNumber("JC001"))
                .thenReturn(Optional.empty());

        when(jockeyRepository.save(any(Jockey.class)))
                .thenReturn(saved);

        JockeyDTO result = jockeyService.createJockey(dto);

        assertNotNull(result);
        assertEquals("jockey01", result.getId());
        assertEquals("Nguyễn Văn A", result.getName());
        assertEquals("JC001", result.getLicenseNumber());
        assertEquals(5, result.getExperienceYears());
        assertEquals(8.5, result.getRating());

        verify(jockeyRepository).findByLicenseNumber("JC001");
        verify(jockeyRepository).save(any(Jockey.class));
    }

    @Test
    void createJockey_duplicateLicenseNumber_throwException() {
        Jockey existing = new Jockey(
                "jockey01",
                "Nguyễn Văn B",
                "JC001",
                3,
                7.5,
                "user02");

        JockeyDTO dto = new JockeyDTO(
                null,
                "Nguyễn Văn A",
                "JC001",
                5,
                8.5,
                "user01");

        when(jockeyRepository.findByLicenseNumber("JC001"))
                .thenReturn(Optional.of(existing));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> jockeyService.createJockey(dto));

        assertEquals("License number already exists", exception.getMessage());

        verify(jockeyRepository, never()).save(any(Jockey.class));
    }
}