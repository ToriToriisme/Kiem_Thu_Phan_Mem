package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.dto.RegistrationDTO;
import com.example.horse_racing_management.entity.Registration;
import com.example.horse_racing_management.entity.Race;
import com.example.horse_racing_management.entity.User;
import com.example.horse_racing_management.entity.enums.RegistrationStatus;
import com.example.horse_racing_management.repository.RaceRepository;
import com.example.horse_racing_management.repository.UserRepository;
import com.example.horse_racing_management.repository.RegistrationRepository;
import com.example.horse_racing_management.repository.HorseRepository;
import com.example.horse_racing_management.repository.TournamentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/management")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminApprovalController {
    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HorseRepository horseRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    // ==========================================
    // 1. API DUYỆT ĐƠN ĐĂNG KÝ
    // ==========================================

    @GetMapping("/registrations/pending")
    public ResponseEntity<?> getPendingRegistrations() {
        // Lấy các đơn Jockey đã ACCEPTED và Admin chưa duyệt (PENDING)
        List<Registration> pendingList = registrationRepository.findAll().stream()
                .filter(reg -> reg.getStatus() == RegistrationStatus.APPROVED &&
                               (reg.getAdminStatus() == null || reg.getAdminStatus() == RegistrationStatus.PENDING))
                .collect(Collectors.toList());

        List<RegistrationDTO> dtoList = pendingList.stream().map(reg -> {
            RegistrationDTO dto = new RegistrationDTO();
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
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }


    @PutMapping("/registrations/{id}/approve")
    public ResponseEntity<?> approveRegistration(@PathVariable String id) {
        try {
            Optional<Registration> opt = registrationRepository.findById(id);
            if (opt.isPresent()) {
                Registration reg = opt.get();
                reg.setAdminStatus(RegistrationStatus.APPROVED);
                registrationRepository.save(reg);
                return ResponseEntity.ok(Map.of("message", "Đã duyệt đơn đăng ký thành công!"));
            }
            return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy đơn đăng ký!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi: " + e.getMessage()));
        }
    }

    @PutMapping("/registrations/{id}/reject")
    public ResponseEntity<?> rejectRegistration(@PathVariable String id) {
        try {
            Optional<Registration> opt = registrationRepository.findById(id);
            if (opt.isPresent()) {
                Registration reg = opt.get();
                reg.setAdminStatus(RegistrationStatus.REJECTED);
                registrationRepository.save(reg);
                return ResponseEntity.ok(Map.of("message", "Đã từ chối đơn đăng ký!"));
            }
            return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy đơn đăng ký!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi: " + e.getMessage()));
        }
    }

    // ==========================================
    // 2. API PHÂN CÔNG TRỌNG TÀI
    // ==========================================

    @GetMapping("/referees")
    public ResponseEntity<?> getAllReferees() {
        // Lọc user có role là ROLE_RACE_REFEREE
        List<User> referees = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "ROLE_RACE_REFEREE".equals(u.getRole().getKey()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(referees);
    }

    @PutMapping("/races/{raceId}/assign-referee/{refereeId}")
    public ResponseEntity<?> assignRefereeToRace(
            @PathVariable String raceId,
            @PathVariable String refereeId) {
        try {
            Optional<Race> raceOpt = raceRepository.findById(raceId);
            Optional<User> refOpt = userRepository.findById(refereeId);

            if (raceOpt.isPresent() && refOpt.isPresent()) {
                Race race = raceOpt.get();
                User referee = refOpt.get();

                // Giả định Race entity có trường refereeId hoặc một đối tượng User referee
                race.setRefereeId(referee.getId());
                raceRepository.save(race);
                return ResponseEntity
                        .ok(Map.of("message", "Đã phân công trọng tài " + referee.getFullName() + " thành công!"));
            }
            return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy chặng đua hoặc trọng tài!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }
}