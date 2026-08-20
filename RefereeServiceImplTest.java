package com.example.horse_racing_management.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.horse_racing_management.entity.HorseHealthCheck;
import com.example.horse_racing_management.entity.RaceResult;
import com.example.horse_racing_management.entity.Registration;
import com.example.horse_racing_management.entity.Violation;
import com.example.horse_racing_management.repository.HorseHealthCheckRepository;
import com.example.horse_racing_management.repository.HorseRepository;
import com.example.horse_racing_management.repository.JockeyRepository;
import com.example.horse_racing_management.repository.RaceResultRepository;
import com.example.horse_racing_management.repository.RefereeReportRepository;
import com.example.horse_racing_management.repository.RegistrationRepository;
import com.example.horse_racing_management.repository.UserRepository;
import com.example.horse_racing_management.repository.ViolationRepository;
import com.example.horse_racing_management.service.impl.RefereeServiceImpl;

@ExtendWith(MockitoExtension.class)
class RefereeServiceImplTest {

        @Mock
        private RaceService raceService;

        @Mock
        private RefereeReportRepository reportRepository;

        @Mock
        private RaceResultRepository resultRepository;

        @Mock
        private ViolationRepository violationRepository;

        @Mock
        private HorseHealthCheckRepository healthCheckRepository;

        @Mock
        private RegistrationRepository registrationRepository;

        @Mock
        private HorseRepository horseRepository;

        @Mock
        private JockeyRepository jockeyRepository;

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private RefereeServiceImpl refereeService;

        @Test
        void createRaceResult_success() {
                RaceResult savedResult = new RaceResult();
                savedResult.setId("result01");
                savedResult.setRaceId("race01");
                savedResult.setHorseId("horse01");
                savedResult.setJockeyId("jockey01");
                savedResult.setPosition(1);
                savedResult.setFinishTime(65.5);
                savedResult.setPrizeMoney(1000.0);

                when(resultRepository.findByRaceIdAndHorseId(
                                "race01", "horse01"))
                                .thenReturn(Optional.empty());

                when(resultRepository.save(any(RaceResult.class)))
                                .thenReturn(savedResult);

                var result = refereeService.createRaceResult(
                                "race01",
                                "horse01",
                                "jockey01",
                                1,
                                65.5);

                assertNotNull(result);
                assertEquals("race01", result.getRaceId());
                assertEquals("horse01", result.getHorseId());
                assertEquals(1, result.getPosition());
                assertEquals(65.5, result.getFinishTime());
                assertEquals(1000.0, result.getPrizeMoney());

                verify(resultRepository).save(any(RaceResult.class));
        }

        @Test
        void createRaceResult_duplicate_throwsException() {
                RaceResult existingResult = new RaceResult();
                existingResult.setId("result01");

                when(resultRepository.findByRaceIdAndHorseId(
                                "race01", "horse01"))
                                .thenReturn(Optional.of(existingResult));

                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> refereeService.createRaceResult(
                                                "race01",
                                                "horse01",
                                                "jockey01",
                                                1,
                                                65.5));

                assertEquals(
                                "Race result already exists for this horse in this race",
                                exception.getMessage());

                verify(resultRepository, never()).save(any());
        }

        @Test
        void recordViolation_success() {
                Violation savedViolation = new Violation();
                savedViolation.setId("violation01");
                savedViolation.setRaceId("race01");
                savedViolation.setHorseId("horse01");
                savedViolation.setJockeyId("jockey01");
                savedViolation.setViolationType("FOUL");
                savedViolation.setDescription("Va cham");
                savedViolation.setPenalty(100.0);
                savedViolation.setSeverity("HIGH");
                savedViolation.setRefereeId("referee01");

                when(violationRepository.save(any(Violation.class)))
                                .thenReturn(savedViolation);

                var result = refereeService.recordViolation(
                                "race01",
                                "horse01",
                                "jockey01",
                                "FOUL",
                                "Va cham",
                                100.0,
                                "HIGH",
                                "referee01");

                assertNotNull(result);
                assertEquals("race01", result.getRaceId());
                assertEquals("horse01", result.getHorseId());
                assertEquals("jockey01", result.getJockeyId());
                assertEquals("FOUL", result.getViolationType());
                assertEquals("Va cham", result.getDescription());
                assertEquals(100.0, result.getPenalty());
                assertEquals("HIGH", result.getSeverity());
                assertEquals("referee01", result.getRefereeId());

                verify(violationRepository).save(any(Violation.class));
        }

        @Test
        void checkHorseHealth_existingHealthCheck() {
                HorseHealthCheck healthCheck = new HorseHealthCheck();
                healthCheck.setHorseId("horse01");
                healthCheck.setRaceId("race01");
                healthCheck.setStatus("HEALTHY");
                healthCheck.setApproved(true);

                when(healthCheckRepository.findByHorseIdAndRaceId(
                                "horse01", "race01"))
                                .thenReturn(Optional.of(healthCheck));

                var result = refereeService.checkHorseHealth(
                                "horse01", "race01");

                assertNotNull(result);
                assertEquals("horse01", result.getHorseId());
                assertEquals("race01", result.getRaceId());
                assertEquals("HEALTHY", result.getStatus());
                assertTrue(result.getApproved());

                verify(healthCheckRepository, never()).save(any());
        }

        @Test
        void checkHorseHealth_noExistingCheck_returnsDefaultHealthy() {
                when(healthCheckRepository.findByHorseIdAndRaceId(
                                "horse01", "race01"))
                                .thenReturn(Optional.empty());

                var result = refereeService.checkHorseHealth(
                                "horse01", "race01");

                assertNotNull(result);
                assertEquals("horse01", result.getHorseId());
                assertEquals("race01", result.getRaceId());
                assertEquals("HEALTHY", result.getStatus());
                assertTrue(result.getApproved());

                verify(healthCheckRepository, never()).save(any());
        }
}