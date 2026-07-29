package com.example.horse_racing_management.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.horse_racing_management.dto.JockeyDTO;
import com.example.horse_racing_management.dto.JockeyScheduleDTO;
import com.example.horse_racing_management.entity.Horse;
import com.example.horse_racing_management.entity.Jockey;
import com.example.horse_racing_management.entity.Race;
import com.example.horse_racing_management.entity.Registration;
import com.example.horse_racing_management.entity.Tournament;
import com.example.horse_racing_management.entity.enums.RegistrationStatus;
import com.example.horse_racing_management.repository.HorseRepository;
import com.example.horse_racing_management.repository.JockeyRepository;
import com.example.horse_racing_management.repository.RaceRepository;
import com.example.horse_racing_management.repository.RegistrationRepository;
import com.example.horse_racing_management.repository.TournamentRepository;
import com.example.horse_racing_management.service.RefereeService;
import com.example.horse_racing_management.service.RegistrationService;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private JockeyRepository jockeyRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private HorseRepository horseRepository;

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private RefereeService refereeService;

    @Override
    public List<JockeyDTO> getJockeysByHorseId(String horseId) {
        List<Registration> registrations = registrationRepository.findByHorseId(horseId);
        List<String> jockeyIds = registrations.stream()
                .map(Registration::getJockeyId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        return jockeyRepository.findAllById(jockeyIds).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void assignJockeyToRegistration(String registrationId, String jockeyId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));
        // ✅ THÊM MỚI: Kiểm tra nếu đã có Jockey
        if (registration.getJockeyId() != null && !registration.getJockeyId().isEmpty()) {
            throw new RuntimeException("Registration đã có Jockey, không thể gán lại.");
        }
        jockeyRepository.findById(jockeyId)
                .orElseThrow(() -> new RuntimeException("Jockey not found"));
        registration.setJockeyId(jockeyId);
        registrationRepository.save(registration);
    }

    @Override
    public List<JockeyScheduleDTO> getScheduleByJockeyId(String jockeyId) {
        List<Registration> registrations = registrationRepository.findByJockeyId(jockeyId);
        return registrations.stream().map(this::buildScheduleDTO).collect(Collectors.toList());
    }

    @Override
    public List<JockeyScheduleDTO> getOwnerRegistrations(String ownerId) {
        List<Horse> ownerHorses = horseRepository.findByOwnerId(ownerId);
        List<String> horseIds = ownerHorses.stream().map(Horse::getId).collect(Collectors.toList());
        List<Registration> registrations = registrationRepository.findByHorseIdIn(horseIds);

        return registrations.stream().map(this::buildScheduleDTO).collect(Collectors.toList());
    }

    private JockeyScheduleDTO buildScheduleDTO(Registration reg) {
        JockeyScheduleDTO dto = new JockeyScheduleDTO();
        dto.setRegistrationId(reg.getId());
        dto.setStatus(reg.getStatus() != null ? reg.getStatus().name() : "PENDING");
        dto.setAdminStatus(reg.getAdminStatus() != null ? reg.getAdminStatus().name() : "PENDING");

        String registrationRef = reg.getRaceId();
        Race race = raceRepository.findById(registrationRef).orElse(null);
        if (race != null) {
            dto.setRaceId(race.getId());
            dto.setRaceName(race.getName());
            if (race.getTournamentId() != null && !race.getTournamentId().isEmpty()) {
                Tournament tournament = tournamentRepository.findById(race.getTournamentId())
                        .orElseThrow(() -> new RuntimeException("Tournament not found for id: " + race.getTournamentId()));
                dto.setTournamentId(tournament.getId());
                dto.setTournamentName(tournament.getName());
                dto.setTournamentStatus(tournament.getStatus().name());
                dto.setStartDate(tournament.getStartDate().toString());
                dto.setEndDate(tournament.getEndDate().toString());
            }
            dto.setRaceResults(refereeService.getRaceResults(race.getId()));
        } else {
            Tournament tournament = tournamentRepository.findById(registrationRef)
                    .orElseThrow(() -> new RuntimeException("Tournament not found for id: " + registrationRef));
            dto.setTournamentId(tournament.getId());
            dto.setTournamentName(tournament.getName());
            dto.setTournamentStatus(tournament.getStatus().name());
            dto.setStartDate(tournament.getStartDate().toString());
            dto.setEndDate(tournament.getEndDate().toString());
            dto.setRaceResults(getRaceResultsForTournament(tournament.getId()));
        }

        Horse horse = horseRepository.findById(reg.getHorseId())
                .orElseThrow(() -> new RuntimeException("Horse not found for id: " + reg.getHorseId()));
        dto.setHorseId(horse.getId());
        dto.setHorseName(horse.getName());

        if (reg.getJockeyId() != null && !reg.getJockeyId().isEmpty()) {
            dto.setJockeyId(reg.getJockeyId());
            jockeyRepository.findById(reg.getJockeyId()).ifPresent(jockey -> dto.setJockeyName(jockey.getName()));
        }

        return dto;
    }

    private List<com.example.horse_racing_management.dto.RaceResultDTO> getRaceResultsForTournament(String tournamentId) {
        return raceRepository.findByTournamentIdOrderByStartTimeAsc(tournamentId).stream()
                .flatMap(race -> refereeService.getRaceResults(race.getId()).stream())
                .collect(Collectors.toList());
    }

    @Override
    public void approveRegistrationByJockey(String registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));
        if (registration.getStatus() != null && registration.getStatus() == RegistrationStatus.APPROVED) {
            throw new RuntimeException("Registration đã được duyệt");
        }
        registration.setStatus(RegistrationStatus.APPROVED);
        registrationRepository.save(registration);
    }

    @Override
    public void rejectRegistrationByJockey(String registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));
        if (registration.getStatus() != null && registration.getStatus() == RegistrationStatus.REJECTED) {
            throw new RuntimeException("Registration đã bị từ chối");
        }
        registration.setStatus(RegistrationStatus.REJECTED);
        registrationRepository.save(registration);
    }

    @Override
    public List<com.example.horse_racing_management.dto.RegistrationDTO> getRegistrationsByTournamentId(String tournamentId) {
        List<Registration> registrations = registrationRepository.findByRaceId(tournamentId);
        return registrations.stream().map(reg -> {
            com.example.horse_racing_management.dto.RegistrationDTO dto = new com.example.horse_racing_management.dto.RegistrationDTO();
            dto.setId(reg.getId());
            dto.setRaceId(reg.getRaceId());
            dto.setHorseId(reg.getHorseId());
            dto.setJockeyId(reg.getJockeyId());
            dto.setRegistrationDate(reg.getRegistrationDate());
            dto.setStatus(reg.getStatus());
            dto.setAdminStatus(reg.getAdminStatus());
            if (reg.getHorseId() != null) {
                horseRepository.findById(reg.getHorseId()).ifPresent(dto::setHorse);
            }
            if (reg.getRaceId() != null) {
                tournamentRepository.findById(reg.getRaceId()).ifPresent(dto::setTournament);
            }
            if (reg.getJockeyId() != null && !reg.getJockeyId().isEmpty()) {
                jockeyRepository.findById(reg.getJockeyId()).ifPresent(dto::setJockey);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private JockeyDTO convertToDTO(Jockey jockey) {
        return new JockeyDTO(jockey.getId(), jockey.getName(), jockey.getLicenseNumber(),
                jockey.getExperienceYears(), jockey.getRating(), jockey.getUserId());
    }
}