package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.dto.RegisterTournamentDTO;
import com.example.horse_racing_management.entity.Registration;
import com.example.horse_racing_management.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/registrations")
@CrossOrigin(origins = "*") // Giúp ReactJS kết nối API thoải mái không lo lỗi bảo mật CORS
public class TournamentRegistrationController {

    @Autowired
    private TournamentService tournamentService;


    @PostMapping("/register")
    public ResponseEntity<?> registerTournament(@RequestBody RegisterTournamentDTO dto) {
        try {
            Registration result = tournamentService.registerHorseToTournament(dto);
            return ResponseEntity.ok(result); // Trả về thông tin đăng ký thành công (Mã 200)
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // Trả về câu báo lỗi (Mã 400)
        }
    }
}