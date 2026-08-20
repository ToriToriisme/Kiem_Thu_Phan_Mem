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

import com.example.horse_racing_management.entity.Jockey;
import com.example.horse_racing_management.entity.Registration;
import com.example.horse_racing_management.entity.enums.RegistrationStatus;
import com.example.horse_racing_management.repository.HorseRepository;
import com.example.horse_racing_management.repository.JockeyRepository;
import com.example.horse_racing_management.repository.RaceRepository;
import com.example.horse_racing_management.repository.RegistrationRepository;
import com.example.horse_racing_management.repository.TournamentRepository;
import com.example.horse_racing_management.service.impl.RegistrationServiceImpl;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

        @Mock
        private RegistrationRepository registrationRepository;

        @Mock
        private JockeyRepository jockeyRepository;

        @Mock
        private TournamentRepository tournamentRepository;

        @Mock
        private HorseRepository horseRepository;

        @Mock
        private RaceRepository raceRepository;

        @Mock
        private RefereeService refereeService;

        @InjectMocks
        private RegistrationServiceImpl registrationService;

        @Test
        void assignJockeyToRegistration_success() {
                Registration registration = new Registration();
                registration.setId("reg01");

                Jockey jockey = new Jockey();
                jockey.setId("jockey01");

                when(registrationRepository.findById("reg01"))
                                .thenReturn(Optional.of(registration));

                when(jockeyRepository.findById("jockey01"))
                                .thenReturn(Optional.of(jockey));

                registrationService.assignJockeyToRegistration("reg01", "jockey01");

                assertEquals("jockey01", registration.getJockeyId());
                verify(registrationRepository).save(registration);
        }

        @Test
        void assignJockeyToRegistration_registrationNotFound() {
                when(registrationRepository.findById("reg01"))
                                .thenReturn(Optional.empty());

                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> registrationService.assignJockeyToRegistration(
                                                "reg01", "jockey01"));

                assertEquals("Registration not found", exception.getMessage());

                verify(registrationRepository, never()).save(any());
        }

        @Test
        void assignJockeyToRegistration_jockeyNotFound() {
                Registration registration = new Registration();
                registration.setId("reg01");

                when(registrationRepository.findById("reg01"))
                                .thenReturn(Optional.of(registration));

                when(jockeyRepository.findById("jockey01"))
                                .thenReturn(Optional.empty());

                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> registrationService.assignJockeyToRegistration(
                                                "reg01", "jockey01"));

                assertEquals("Jockey not found", exception.getMessage());

                verify(registrationRepository, never()).save(any());
        }

        @Test
        void assignJockeyToRegistration_alreadyHasJockey() {
                Registration registration = new Registration();
                registration.setId("reg01");
                registration.setJockeyId("oldJockey");

                when(registrationRepository.findById("reg01"))
                                .thenReturn(Optional.of(registration));

                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> registrationService.assignJockeyToRegistration(
                                                "reg01", "newJockey"));

                assertEquals(
                                "Registration đã có Jockey, không thể gán lại.",
                                exception.getMessage());

                verify(jockeyRepository, never()).findById(any());
                verify(registrationRepository, never()).save(any());
        }

        @Test
        void approveRegistrationByJockey_success() {
                Registration registration = new Registration();
                registration.setId("reg01");
                registration.setStatus(RegistrationStatus.PENDING);

                when(registrationRepository.findById("reg01"))
                                .thenReturn(Optional.of(registration));

                registrationService.approveRegistrationByJockey("reg01");

                assertEquals(
                                RegistrationStatus.APPROVED,
                                registration.getStatus());

                verify(registrationRepository).save(registration);
        }

        @Test
        void approveRegistrationByJockey_alreadyApproved() {
                Registration registration = new Registration();
                registration.setId("reg01");
                registration.setStatus(RegistrationStatus.APPROVED);

                when(registrationRepository.findById("reg01"))
                                .thenReturn(Optional.of(registration));

                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> registrationService.approveRegistrationByJockey("reg01"));

                assertEquals(
                                "Registration đã được duyệt",
                                exception.getMessage());

                verify(registrationRepository, never()).save(any());
        }

        @Test
        void rejectRegistrationByJockey_success() {
                Registration registration = new Registration();
                registration.setId("reg01");
                registration.setStatus(RegistrationStatus.PENDING);

                when(registrationRepository.findById("reg01"))
                                .thenReturn(Optional.of(registration));

                registrationService.rejectRegistrationByJockey("reg01");

                assertEquals(
                                RegistrationStatus.REJECTED,
                                registration.getStatus());

                verify(registrationRepository).save(registration);
        }

        @Test
        void rejectRegistrationByJockey_alreadyRejected() {
                Registration registration = new Registration();
                registration.setId("reg01");
                registration.setStatus(RegistrationStatus.REJECTED);

                when(registrationRepository.findById("reg01"))
                                .thenReturn(Optional.of(registration));

                RuntimeException exception = assertThrows(
                                RuntimeException.class,
                                () -> registrationService.rejectRegistrationByJockey("reg01"));

                assertEquals(
                                "Registration đã bị từ chối",
                                exception.getMessage());

                verify(registrationRepository, never()).save(any());
        }

        @Test
        void getJockeysByHorseId_success() {
                Registration r1 = new Registration();
                r1.setJockeyId("jockey01");

                Registration r2 = new Registration();
                r2.setJockeyId("jockey01");

                Registration r3 = new Registration();
                r3.setJockeyId("jockey02");

                when(registrationRepository.findByHorseId("horse01"))
                                .thenReturn(List.of(r1, r2, r3));

                Jockey jockey1 = new Jockey();
                jockey1.setId("jockey01");
                jockey1.setName("Nguyen Van A");

                Jockey jockey2 = new Jockey();
                jockey2.setId("jockey02");
                jockey2.setName("Tran Van B");

                when(jockeyRepository.findAllById(
                                List.of("jockey01", "jockey02")))
                                .thenReturn(List.of(jockey1, jockey2));

                var result = registrationService.getJockeysByHorseId("horse01");

                assertEquals(2, result.size());
                assertEquals("jockey01", result.get(0).getId());
                assertEquals("jockey02", result.get(1).getId());
        }
}