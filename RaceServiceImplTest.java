package com.example.horse_racing_management.service;

import com.example.horse_racing_management.dto.RaceDTO;
import com.example.horse_racing_management.entity.Race;
import com.example.horse_racing_management.entity.Tournament;
import com.example.horse_racing_management.entity.enums.RaceStatus;
import com.example.horse_racing_management.repository.RaceRepository;
import com.example.horse_racing_management.repository.TournamentRepository;
import com.example.horse_racing_management.repository.UserRepository;
import com.example.horse_racing_management.service.impl.RaceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RaceServiceImplTest {

    @Mock
    private RaceRepository raceRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RaceServiceImpl raceService;

    @Test
    void createRace_success() {

        RaceDTO dto = new RaceDTO(
                null,
                "tournament01",
                null,
                "Chặng 1",
                LocalDateTime.now(),
                1200.0,
                RaceStatus.SCHEDULED,
                null,
                null,
                5);

        Race savedRace = new Race();

        savedRace.setId("race01");
        savedRace.setTournamentId("tournament01");
        savedRace.setName("Chặng 1");
        savedRace.setStartTime(dto.getStartTime());
        savedRace.setDistance(1200.0);
        savedRace.setStatus(RaceStatus.SCHEDULED);
        savedRace.setAdvancingCount(5);

        when(tournamentRepository.existsById("tournament01"))
                .thenReturn(true);

        when(raceRepository.save(any(Race.class)))
                .thenReturn(savedRace);

        when(tournamentRepository.findById("tournament01"))
                .thenReturn(Optional.of(
                        createTournament()));

        RaceDTO result = raceService.createRace(dto);

        assertNotNull(result);
        assertEquals("race01", result.getId());
        assertEquals("tournament01", result.getTournamentId());
        assertEquals("Chặng 1", result.getName());
        assertEquals(1200.0, result.getDistance());
        assertEquals(RaceStatus.SCHEDULED, result.getStatus());

        verify(tournamentRepository)
                .existsById("tournament01");

        verify(raceRepository)
                .save(any(Race.class));
    }

    @Test
    void createRace_tournamentNotFound_throwException() {

        RaceDTO dto = new RaceDTO(
                null,
                "invalid",
                null,
                "Chặng 1",
                LocalDateTime.now(),
                1200.0,
                RaceStatus.SCHEDULED,
                null,
                null,
                5);

        when(tournamentRepository.existsById("invalid"))
                .thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> raceService.createRace(dto));

        assertEquals(
                "Không tìm thấy giải đấu với id: invalid",
                exception.getMessage());

        verify(raceRepository, never())
                .save(any(Race.class));
    }

    @Test
    void createRace_emptyName_throwException() {

        RaceDTO dto = new RaceDTO(
                null,
                "tournament01",
                null,
                "",
                LocalDateTime.now(),
                1200.0,
                RaceStatus.SCHEDULED,
                null,
                null,
                5);

        when(tournamentRepository.existsById("tournament01"))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> raceService.createRace(dto));

        assertEquals(
                "Tên cuộc đua không được để trống.",
                exception.getMessage());

        verify(raceRepository, never())
                .save(any(Race.class));
    }

    @Test
    void createRace_invalidDistance_throwException() {

        RaceDTO dto = new RaceDTO(
                null,
                "tournament01",
                null,
                "Chặng 1",
                LocalDateTime.now(),
                0.0,
                RaceStatus.SCHEDULED,
                null,
                null,
                5);

        when(tournamentRepository.existsById("tournament01"))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> raceService.createRace(dto));

        assertEquals(
                "Quãng đường cuộc đua phải lớn hơn 0.",
                exception.getMessage());

        verify(raceRepository, never())
                .save(any(Race.class));
    }

    private Tournament createTournament() {
        Tournament tournament = new Tournament();
        tournament.setId("tournament01");
        tournament.setName("Giải Đua Mùa Hè");
        return tournament;
    }
}