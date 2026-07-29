package com.example.horse_racing_management.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.horse_racing_management.dto.HorseHealthCheckDTO;
import com.example.horse_racing_management.dto.HorseParticipantDTO;
import com.example.horse_racing_management.dto.RaceResultDTO;
import com.example.horse_racing_management.dto.RaceTrackingDTO;
import com.example.horse_racing_management.dto.RefereeReportDTO;
import com.example.horse_racing_management.dto.ViolationDTO;
import com.example.horse_racing_management.entity.Horse;
import com.example.horse_racing_management.entity.HorseHealthCheck;
import com.example.horse_racing_management.entity.Jockey;
import com.example.horse_racing_management.entity.Race;
import com.example.horse_racing_management.entity.RaceResult;
import com.example.horse_racing_management.entity.RefereeReport;
import com.example.horse_racing_management.entity.Registration;
import com.example.horse_racing_management.entity.User;
import com.example.horse_racing_management.entity.Violation;
import com.example.horse_racing_management.repository.HorseHealthCheckRepository;
import com.example.horse_racing_management.repository.HorseRepository;
import com.example.horse_racing_management.repository.JockeyRepository;
import com.example.horse_racing_management.repository.RaceResultRepository;
import com.example.horse_racing_management.repository.RefereeReportRepository;
import com.example.horse_racing_management.repository.RegistrationRepository;
import com.example.horse_racing_management.repository.UserRepository;
import com.example.horse_racing_management.repository.ViolationRepository;
import com.example.horse_racing_management.service.RaceService;
import com.example.horse_racing_management.service.RefereeService;

@Service
@Transactional
public class RefereeServiceImpl implements RefereeService {

    @Autowired
    private RaceService raceService;

    @Autowired
    private RefereeReportRepository reportRepository;

    @Autowired
    private RaceResultRepository resultRepository;

    @Autowired
    private ViolationRepository violationRepository;

    @Autowired
    private HorseHealthCheckRepository healthCheckRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private HorseRepository horseRepository;

    @Autowired
    private JockeyRepository jockeyRepository;

    @Autowired
    private UserRepository userRepository;

    // ==================== REFEREE REPORT MANAGEMENT ====================

    @Override
    public List<com.example.horse_racing_management.dto.RaceDTO> getAssignedRaces(String refereeId) {
        // Get all races assigned to this referee
        return raceService.getRacesByRefereeId(refereeId);
    }

    @Override
    public RaceTrackingDTO getRaceDetails(String raceId) {
        Race race = raceService.getRaceEntity(raceId)
                .orElseThrow(() -> new RuntimeException("Race not found: " + raceId));

        RaceTrackingDTO dto = new RaceTrackingDTO();
        dto.setRaceId(race.getId());
        dto.setRaceName(race.getName());
        dto.setStartTime(race.getStartTime());
        dto.setDistance(race.getDistance());
        dto.setStatus(race.getStatus().toString());
        dto.setLocation("Default Location"); // TODO: add location to Race entity

        // Get participants
        List<HorseParticipantDTO> participants = getHorsesByRace(raceId);
        dto.setParticipants(participants);

        return dto;
    }

    @Override
    public List<HorseParticipantDTO> getHorsesByRace(String raceId) {
        // Get all registrations for this race
        List<Registration> registrations = registrationRepository.findByRaceId(raceId);

        if (registrations.isEmpty()) {
            // Fallback for current data model: some registrations are stored against tournamentId
            raceService.getRaceEntity(raceId).ifPresent(race -> {
                if (race.getTournamentId() != null && !race.getTournamentId().isBlank()) {
                    registrations.addAll(registrationRepository.findByRaceId(race.getTournamentId()));
                }
            });
        }

        return registrations.stream()
                .map(reg -> {
                    HorseParticipantDTO dto = new HorseParticipantDTO();
                    dto.setRegistrationStatus(reg.getStatus().toString());

                    // Get horse info
                    if (reg.getHorseId() != null) {
                        Horse horse = horseRepository.findById(reg.getHorseId()).orElse(null);
                        if (horse != null) {
                            dto.setHorseId(horse.getId());
                            dto.setHorseName(horse.getName());
                            dto.setBreed(horse.getBreed());
                            dto.setAge(horse.getAge());
                            dto.setOwnerId(horse.getOwnerId());

                            // Get owner name
                            User owner = userRepository.findById(horse.getOwnerId()).orElse(null);
                            if (owner != null) {
                                dto.setOwnerName(owner.getFullName());
                            }
                        }
                    }

                    // Get jockey info
                    if (reg.getJockeyId() != null) {
                        Jockey jockey = jockeyRepository.findById(reg.getJockeyId()).orElse(null);
                        if (jockey != null) {
                            dto.setJockeyId(jockey.getId());
                            dto.setJockeyName(jockey.getName());
                            dto.setJockeyLicense(jockey.getLicenseNumber());
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public RefereeReportDTO createReport(String refereeId, String raceId, String reportText) {
        RefereeReport report = new RefereeReport();
        report.setRefereeId(refereeId);
        report.setRaceId(raceId);
        report.setReportText(reportText);
        report.setCreatedAt(LocalDateTime.now());

        RefereeReport saved = reportRepository.save(report);
        return convertToDTO(saved);
    }

    @Override
    public RefereeReportDTO updateReport(String reportId, String reportText, Long version) {
        RefereeReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

        if (!report.getVersion().equals(version)) {
            throw new OptimisticLockingFailureException(
                    "Report has been updated by another process. Please refresh and try again."
            );
        }

        report.setReportText(reportText);
        RefereeReport updated = reportRepository.save(report);
        return convertToDTO(updated);
    }

    @Override
    public RefereeReportDTO getReportById(String reportId) {
        return reportRepository.findById(reportId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));
    }

    @Override
    public List<RefereeReportDTO> getReportsByReferee(String refereeId) {
        return reportRepository.findByRefereeId(refereeId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefereeReportDTO> getReportsByRace(String raceId) {
        return reportRepository.findByRaceId(raceId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== RACE RESULTS MANAGEMENT ====================

    @Override
    public RaceResultDTO createRaceResult(String raceId, String horseId, String jockeyId,
                                         Integer position, Double finishTime) {
        // Check if result already exists
        resultRepository.findByRaceIdAndHorseId(raceId, horseId).ifPresent(existing -> {
            throw new RuntimeException("Race result already exists for this horse in this race");
        });

        RaceResult result = new RaceResult();
        result.setRaceId(raceId);
        result.setHorseId(horseId);
        result.setJockeyId(jockeyId);
        result.setPosition(position);
        result.setFinishTime(finishTime);
        result.setPrizeMoney(calculatePrizeMoney(position));

        RaceResult saved = resultRepository.save(result);
        return convertToDTO(saved);
    }

    @Override
    public RaceResultDTO updateRaceResult(String resultId, Integer position, Double finishTime,
                                         Double prizeMoney, Long version) {
        RaceResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Race result not found: " + resultId));

        if (!result.getVersion().equals(version)) {
            throw new OptimisticLockingFailureException(
                    "Race result has been updated by another process. Please refresh and try again."
            );
        }

        Integer oldPosition = result.getPosition();

        result.setPosition(position);
        result.setFinishTime(finishTime);
        result.setPrizeMoney(prizeMoney);

        RaceResult updated = resultRepository.save(result);

        // If the horse was disqualified (moved to position 99), shift others up
        if (oldPosition != null && oldPosition != 99 && position == 99) {
            List<RaceResult> allResults = resultRepository.findByRaceId(result.getRaceId());
            for (RaceResult other : allResults) {
                if (!other.getId().equals(resultId) && other.getPosition() != 99 && other.getPosition() > oldPosition) {
                    other.setPosition(other.getPosition() - 1);
                    resultRepository.save(other);
                }
            }
        }

        return convertToDTO(updated);
    }

    @Override
    public List<RaceResultDTO> getRaceResults(String raceId) {
        return resultRepository.findByRaceId(raceId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RaceResultDTO getRaceResultById(String resultId) {
        return resultRepository.findById(resultId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Race result not found: " + resultId));
    }

    // ==================== VIOLATIONS MANAGEMENT ====================

    @Override
    public ViolationDTO recordViolation(String raceId, String horseId, String jockeyId,
                                       String violationType, String description, Double penalty,
                                       String severity, String refereeId) {
        Violation violation = new Violation();
        violation.setRaceId(raceId);
        violation.setHorseId(horseId);
        violation.setJockeyId(jockeyId);
        violation.setViolationType(violationType);
        violation.setDescription(description);
        violation.setPenalty(penalty);
        violation.setSeverity(severity);
        violation.setRefereeId(refereeId);
        violation.setRecordedAt(LocalDateTime.now());

        Violation saved = violationRepository.save(violation);
        return convertToDTO(saved);
    }

    @Override
    public ViolationDTO updateViolation(String violationId, String description,
                                       Double penalty, String severity, Long version) {
        Violation violation = violationRepository.findById(violationId)
                .orElseThrow(() -> new RuntimeException("Violation not found: " + violationId));

        if (!violation.getVersion().equals(version)) {
            throw new OptimisticLockingFailureException(
                    "Violation has been updated by another process. Please refresh and try again."
            );
        }

        violation.setDescription(description);
        violation.setPenalty(penalty);
        violation.setSeverity(severity);

        Violation updated = violationRepository.save(violation);
        return convertToDTO(updated);
    }

    @Override
    public List<ViolationDTO> getViolationsByRace(String raceId) {
        return violationRepository.findByRaceId(raceId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ViolationDTO> getViolationsByHorse(String horseId) {
        return violationRepository.findByHorseId(horseId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ViolationDTO getViolationById(String violationId) {
        return violationRepository.findById(violationId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Violation not found: " + violationId));
    }

    // ==================== HORSE HEALTH CHECK MANAGEMENT ====================

    @Override
    public HorseHealthCheckDTO checkHorseHealth(String horseId, String raceId) {
        HorseHealthCheck healthCheck = healthCheckRepository.findByHorseIdAndRaceId(horseId, raceId)
                .orElseGet(() -> {
                    HorseHealthCheck newCheck = new HorseHealthCheck();
                    newCheck.setHorseId(horseId);
                    newCheck.setRaceId(raceId);
                    newCheck.setStatus("HEALTHY");
                    newCheck.setApproved(true);
                    return newCheck;
                });

        return convertToDTO(healthCheck);
    }

    @Override
    public List<HorseHealthCheckDTO> checkAllHorsesHealth(String raceId) {
        List<HorseHealthCheck> healthChecks = healthCheckRepository.findByRaceId(raceId);
        if (healthChecks.isEmpty()) {
            // Create default health checks for all horses in this race
            List<HorseParticipantDTO> participants = getHorsesByRace(raceId);
            return participants.stream()
                    .map(participant -> {
                        HorseHealthCheck check = new HorseHealthCheck();
                        check.setHorseId(participant.getHorseId());
                        check.setRaceId(raceId);
                        check.setStatus("HEALTHY");
                        check.setApproved(true);
                        HorseHealthCheck saved = healthCheckRepository.save(check);
                        return convertToDTO(saved);
                    })
                    .collect(Collectors.toList());
        }

        return healthChecks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private RefereeReportDTO convertToDTO(RefereeReport report) {
        RefereeReportDTO dto = new RefereeReportDTO();
        dto.setId(report.getId());
        dto.setRaceId(report.getRaceId());
        dto.setRefereeId(report.getRefereeId());
        dto.setReportText(report.getReportText());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setVersion(report.getVersion());

        // Get race name
        if (report.getRaceId() != null) {
            try {
                Race race = raceService.getRaceEntity(report.getRaceId()).orElse(null);
                if (race != null) {
                    dto.setRaceName(race.getName());
                }
            } catch (Exception e) {
                // Log error but continue
            }
        }

        // Get referee name
        if (report.getRefereeId() != null) {
            User referee = userRepository.findById(report.getRefereeId()).orElse(null);
            if (referee != null) {
                dto.setRefereeName(referee.getFullName());
            }
        }

        return dto;
    }

    private RaceResultDTO convertToDTO(RaceResult result) {
        RaceResultDTO dto = new RaceResultDTO();
        dto.setId(result.getId());
        dto.setRaceId(result.getRaceId());
        dto.setHorseId(result.getHorseId());
        dto.setJockeyId(result.getJockeyId());
        dto.setPosition(result.getPosition());
        dto.setFinishTime(result.getFinishTime());
        dto.setPrizeMoney(result.getPrizeMoney());
        dto.setVersion(result.getVersion());

        // Get horse name
        if (result.getHorseId() != null) {
            Horse horse = horseRepository.findById(result.getHorseId()).orElse(null);
            if (horse != null) {
                dto.setHorseName(horse.getName());
            }
        }

        // Get jockey name
        if (result.getJockeyId() != null) {
            Jockey jockey = jockeyRepository.findById(result.getJockeyId()).orElse(null);
            if (jockey != null) {
                dto.setJockeyName(jockey.getName());
            }
        }

        // Get race name
        if (result.getRaceId() != null) {
            try {
                Race race = raceService.getRaceEntity(result.getRaceId()).orElse(null);
                if (race != null) {
                    dto.setRaceName(race.getName());
                }
            } catch (Exception e) {
                // Log error but continue
            }
        }

        return dto;
    }

    private ViolationDTO convertToDTO(Violation violation) {
        ViolationDTO dto = new ViolationDTO();
        dto.setId(violation.getId());
        dto.setRaceId(violation.getRaceId());
        dto.setHorseId(violation.getHorseId());
        dto.setJockeyId(violation.getJockeyId());
        dto.setViolationType(violation.getViolationType());
        dto.setDescription(violation.getDescription());
        dto.setPenalty(violation.getPenalty());
        dto.setSeverity(violation.getSeverity());
        dto.setRefereeId(violation.getRefereeId());
        dto.setRecordedAt(violation.getRecordedAt());
        dto.setVersion(violation.getVersion());

        // Get horse name
        if (violation.getHorseId() != null) {
            Horse horse = horseRepository.findById(violation.getHorseId()).orElse(null);
            if (horse != null) {
                dto.setHorseName(horse.getName());
            }
        }

        // Get jockey name
        if (violation.getJockeyId() != null) {
            Jockey jockey = jockeyRepository.findById(violation.getJockeyId()).orElse(null);
            if (jockey != null) {
                dto.setJockeyName(jockey.getName());
            }
        }

        // Get referee name
        if (violation.getRefereeId() != null) {
            User referee = userRepository.findById(violation.getRefereeId()).orElse(null);
            if (referee != null) {
                dto.setRefereeName(referee.getFullName());
            }
        }

        return dto;
    }

    private HorseHealthCheckDTO convertToDTO(HorseHealthCheck healthCheck) {
        HorseHealthCheckDTO dto = new HorseHealthCheckDTO();
        dto.setHorseId(healthCheck.getHorseId());
        dto.setRaceId(healthCheck.getRaceId());
        dto.setStatus(healthCheck.getStatus());
        dto.setNotes(healthCheck.getNotes());
        dto.setCheckedAt(healthCheck.getCheckedAt());
        dto.setApproved(healthCheck.getApproved());

        // Get horse name
        Horse horse = horseRepository.findById(healthCheck.getHorseId()).orElse(null);
        if (horse != null) {
            dto.setHorseName(horse.getName());
        }

        // Get checked by (referee name)
        if (healthCheck.getCheckedBy() != null) {
            User referee = userRepository.findById(healthCheck.getCheckedBy()).orElse(null);
            if (referee != null) {
                dto.setCheckedBy(referee.getFullName());
            }
        }

        return dto;
    }

    private Double calculatePrizeMoney(Integer position) {
        // TODO: Implement prize money calculation based on race type and position
        return switch (position) {
            case 1 -> 1000.0;
            case 2 -> 600.0;
            case 3 -> 400.0;
            default -> 100.0;
        };
    }
}
