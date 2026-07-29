package com.example.horse_racing_management.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.horse_racing_management.dto.HorseHealthCheckDTO;
import com.example.horse_racing_management.dto.HorseParticipantDTO;
import com.example.horse_racing_management.dto.RaceResultDTO;
import com.example.horse_racing_management.dto.RaceTrackingDTO;
import com.example.horse_racing_management.dto.RefereeReportDTO;
import com.example.horse_racing_management.dto.ViolationDTO;
import com.example.horse_racing_management.service.RefereeService;

/**
 * REST Controller for Referee operations including race assignment, health checks,
 * violations, race results, and report management.
 */
@RestController
@RequestMapping("/api/referee")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RefereeController {

    @Autowired
    private RefereeService refereeService;

    // ==================== ASSIGNED RACES ====================

    @GetMapping("/{refereeId}/assigned-races")
    public ResponseEntity<?> getAssignedRaces(@PathVariable String refereeId) {
        try {
            List<com.example.horse_racing_management.dto.RaceDTO> races = refereeService.getAssignedRaces(refereeId);
            return ResponseEntity.ok(races);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/race/{raceId}/details")
    public ResponseEntity<?> getRaceDetails(@PathVariable String raceId) {
        try {
            RaceTrackingDTO raceDetails = refereeService.getRaceDetails(raceId);
            return ResponseEntity.ok(raceDetails);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/race/{raceId}/horses")
    public ResponseEntity<?> getHorsesByRace(@PathVariable String raceId) {
        try {
            List<HorseParticipantDTO> horses = refereeService.getHorsesByRace(raceId);
            return ResponseEntity.ok(horses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== REFEREE REPORTS ====================

    @PostMapping("/{refereeId}/report")
    public ResponseEntity<?> createReport(
            @PathVariable String refereeId,
            @RequestParam String raceId,
            @RequestBody Map<String, String> body) {
        try {
            String reportText = body.get("reportText");
            RefereeReportDTO report = refereeService.createReport(refereeId, raceId, reportText);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/report/{reportId}")
    public ResponseEntity<?> updateReport(
            @PathVariable String reportId,
            @RequestBody Map<String, Object> body) {
        try {
            String reportText = (String) body.get("reportText");
            Long version = ((Number) body.get("version")).longValue();
            RefereeReportDTO report = refereeService.updateReport(reportId, reportText, version);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/report/{reportId}")
    public ResponseEntity<?> getReportById(@PathVariable String reportId) {
        try {
            RefereeReportDTO report = refereeService.getReportById(reportId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{refereeId}/reports")
    public ResponseEntity<?> getReportsByReferee(@PathVariable String refereeId) {
        try {
            List<RefereeReportDTO> reports = refereeService.getReportsByReferee(refereeId);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/race/{raceId}/reports")
    public ResponseEntity<?> getReportsByRace(@PathVariable String raceId) {
        try {
            List<RefereeReportDTO> reports = refereeService.getReportsByRace(raceId);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== RACE RESULTS ====================

    @PostMapping("/race/{raceId}/result")
    public ResponseEntity<?> createRaceResult(
            @PathVariable String raceId,
            @RequestBody Map<String, Object> body) {
        try {
            String horseId = (String) body.get("horseId");
            String jockeyId = (String) body.get("jockeyId");
            Integer position = ((Number) body.get("position")).intValue();
            Double finishTime = ((Number) body.get("finishTime")).doubleValue();

            RaceResultDTO result = refereeService.createRaceResult(raceId, horseId, jockeyId, position, finishTime);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/result/{resultId}")
    public ResponseEntity<?> updateRaceResult(
            @PathVariable String resultId,
            @RequestBody Map<String, Object> body) {
        try {
            Integer position = ((Number) body.get("position")).intValue();
            Double finishTime = ((Number) body.get("finishTime")).doubleValue();
            Double prizeMoney = ((Number) body.get("prizeMoney")).doubleValue();
            Long version = ((Number) body.get("version")).longValue();

            RaceResultDTO result = refereeService.updateRaceResult(resultId, position, finishTime, prizeMoney, version);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/race/{raceId}/results")
    public ResponseEntity<?> getRaceResults(@PathVariable String raceId) {
        try {
            List<RaceResultDTO> results = refereeService.getRaceResults(raceId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/result/{resultId}")
    public ResponseEntity<?> getRaceResultById(@PathVariable String resultId) {
        try {
            RaceResultDTO result = refereeService.getRaceResultById(resultId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== VIOLATIONS ====================

    @PostMapping("/race/{raceId}/violation")
    public ResponseEntity<?> recordViolation(
            @PathVariable String raceId,
            @RequestBody Map<String, Object> body) {
        try {
            String horseId = (String) body.get("horseId");
            String jockeyId = (String) body.get("jockeyId");
            String violationType = (String) body.get("violationType");
            String description = (String) body.get("description");
            Double penalty = ((Number) body.get("penalty")).doubleValue();
            String severity = (String) body.get("severity");
            String refereeId = (String) body.get("refereeId");

            ViolationDTO violation = refereeService.recordViolation(
                    raceId, horseId, jockeyId, violationType, description, penalty, severity, refereeId
            );
            return ResponseEntity.ok(violation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/violation/{violationId}")
    public ResponseEntity<?> updateViolation(
            @PathVariable String violationId,
            @RequestBody Map<String, Object> body) {
        try {
            String description = (String) body.get("description");
            Double penalty = ((Number) body.get("penalty")).doubleValue();
            String severity = (String) body.get("severity");
            Long version = ((Number) body.get("version")).longValue();

            ViolationDTO violation = refereeService.updateViolation(violationId, description, penalty, severity, version);
            return ResponseEntity.ok(violation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/race/{raceId}/violations")
    public ResponseEntity<?> getViolationsByRace(@PathVariable String raceId) {
        try {
            List<ViolationDTO> violations = refereeService.getViolationsByRace(raceId);
            return ResponseEntity.ok(violations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/horse/{horseId}/violations")
    public ResponseEntity<?> getViolationsByHorse(@PathVariable String horseId) {
        try {
            List<ViolationDTO> violations = refereeService.getViolationsByHorse(horseId);
            return ResponseEntity.ok(violations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/violation/{violationId}")
    public ResponseEntity<?> getViolationById(@PathVariable String violationId) {
        try {
            ViolationDTO violation = refereeService.getViolationById(violationId);
            return ResponseEntity.ok(violation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== HORSE HEALTH CHECK ====================

    @GetMapping("/horse/{horseId}/health/race/{raceId}")
    public ResponseEntity<?> checkHorseHealth(
            @PathVariable String horseId,
            @PathVariable String raceId) {
        try {
            HorseHealthCheckDTO healthCheck = refereeService.checkHorseHealth(horseId, raceId);
            return ResponseEntity.ok(healthCheck);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/race/{raceId}/health-check/all")
    public ResponseEntity<?> checkAllHorsesHealth(@PathVariable String raceId) {
        try {
            List<HorseHealthCheckDTO> healthChecks = refereeService.checkAllHorsesHealth(raceId);
            return ResponseEntity.ok(healthChecks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
