package com.example.horse_racing_management.service;

import com.example.horse_racing_management.dto.TournamentDTO;
import com.example.horse_racing_management.entity.Tournament;
import com.example.horse_racing_management.entity.enums.TournamentStatus;
import com.example.horse_racing_management.repository.*;
import com.example.horse_racing_management.service.impl.TournamentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TournamentServiceImplTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private RaceRepository raceRepository;

    @Mock
    private HorseRepository horseRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private JockeyRepository jockeyRepository;

    @Mock
    private RaceResultRepository raceResultRepository;

    @InjectMocks
    private TournamentServiceImpl tournamentService;

    @Test
    void createTournament_success() {

        Date startDate = new Date(System.currentTimeMillis());
        Date endDate = new Date(
                System.currentTimeMillis() + 86400000);

        TournamentDTO dto = new TournamentDTO(
                null,
                "Giải Đua Mùa Hè",
                "Giải đua ngựa mùa hè",
                startDate,
                endDate,
                TournamentStatus.UPCOMING);

        Tournament savedTournament = new Tournament();

        savedTournament.setId("tournament01");
        savedTournament.setName("Giải Đua Mùa Hè");
        savedTournament.setDescription("Giải đua ngựa mùa hè");
        savedTournament.setStartDate(startDate);
        savedTournament.setEndDate(endDate);
        savedTournament.setStatus(TournamentStatus.UPCOMING);

        when(tournamentRepository.save(any(Tournament.class)))
                .thenReturn(savedTournament);

        TournamentDTO result = tournamentService.createTournament(dto);

        assertNotNull(result);
        assertEquals("tournament01", result.getId());
        assertEquals("Giải Đua Mùa Hè", result.getName());
        assertEquals(TournamentStatus.UPCOMING, result.getStatus());

        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void createTournament_emptyName_throwException() {

        Date startDate = new Date();
        Date endDate = new Date(
                System.currentTimeMillis() + 86400000);

        TournamentDTO dto = new TournamentDTO(
                null,
                "",
                "Test",
                startDate,
                endDate,
                TournamentStatus.UPCOMING);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> tournamentService.createTournament(dto));

        assertEquals(
                "Tên giải đấu không được để trống.",
                exception.getMessage());

        verify(tournamentRepository, never())
                .save(any(Tournament.class));
    }

    @Test
    void createTournament_startDateAfterEndDate_throwException() {

        Date startDate = new Date(
                System.currentTimeMillis() + 86400000);

        Date endDate = new Date();

        TournamentDTO dto = new TournamentDTO(
                null,
                "Giải đua",
                "Test",
                startDate,
                endDate,
                TournamentStatus.UPCOMING);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> tournamentService.createTournament(dto));

        assertEquals(
                "Ngày bắt đầu không được sau ngày kết thúc.",
                exception.getMessage());

        verify(tournamentRepository, never())
                .save(any(Tournament.class));
    }
}