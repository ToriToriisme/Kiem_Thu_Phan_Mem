package com.example.horse_racing_management.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.horse_racing_management.dto.JockeyDTO;
import com.example.horse_racing_management.dto.JockeyScheduleDTO;
import com.example.horse_racing_management.service.RegistrationService;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @GetMapping("/horse/{horseId}/jockeys")
    public ResponseEntity<List<JockeyDTO>> getJockeysByHorse(@PathVariable String horseId) {
        return ResponseEntity.ok(registrationService.getJockeysByHorseId(horseId));
    }

    @PutMapping("/{registrationId}/assign-jockey/{jockeyId}")
    public ResponseEntity<Void> assignJockey(@PathVariable String registrationId, @PathVariable String jockeyId) {
        registrationService.assignJockeyToRegistration(registrationId, jockeyId);
        return ResponseEntity.ok().build();

    }
    @GetMapping("/jockey/{jockeyId}/schedule")
    public ResponseEntity<List<JockeyScheduleDTO>> getJockeySchedule(@PathVariable String jockeyId) {
        return ResponseEntity.ok(registrationService.getScheduleByJockeyId(jockeyId));
    }

    @GetMapping("/owner/{ownerId}/requests")
    public ResponseEntity<List<JockeyScheduleDTO>> getOwnerRegistrations(@PathVariable String ownerId) {
        return ResponseEntity.ok(registrationService.getOwnerRegistrations(ownerId));
    }

    @PutMapping("/{registrationId}/approve-by-jockey")
    public ResponseEntity<?> approveRegistrationByJockey(@PathVariable String registrationId) {
        try {
            registrationService.approveRegistrationByJockey(registrationId);
            return ResponseEntity.ok(java.util.Map.of("message", "Đã chấp nhận lịch trình thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Lỗi: " + e.getMessage()));
        }
    }

    @PutMapping("/{registrationId}/reject-by-jockey")
    public ResponseEntity<?> rejectRegistrationByJockey(@PathVariable String registrationId) {
        try {
            registrationService.rejectRegistrationByJockey(registrationId);
            return ResponseEntity.ok(java.util.Map.of("message", "Đã từ chối lịch trình thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Lỗi: " + e.getMessage()));
        }
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<com.example.horse_racing_management.dto.RegistrationDTO>> getRegistrationsByTournament(@PathVariable String tournamentId) {
        return ResponseEntity.ok(registrationService.getRegistrationsByTournamentId(tournamentId));
    }
}