package com.example.horse_racing_management.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.horse_racing_management.dto.RegisterTournamentDTO;
import com.example.horse_racing_management.dto.TournamentDTO;
import com.example.horse_racing_management.service.TournamentService;

@RestController
@RequestMapping("/api/admin/tournaments")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @GetMapping
    public ResponseEntity<List<TournamentDTO>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentDTO> getTournamentById(@PathVariable String id) {
        return ResponseEntity.ok(tournamentService.getTournamentById(id));
    }

    @PostMapping
    public ResponseEntity<?> createTournament(@RequestBody TournamentDTO tournamentDTO) {
        try {
            return ResponseEntity.ok(tournamentService.createTournament(tournamentDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTournament(@PathVariable String id, @RequestBody TournamentDTO tournamentDTO) {
        try {
            return ResponseEntity.ok(tournamentService.updateTournament(id, tournamentDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<?> registerHorseToTournament(
            @PathVariable("id") String tournamentId,
            @RequestBody RegisterTournamentDTO dto) {
        try {
            dto.setTournamentId(tournamentId);
            return ResponseEntity.ok(tournamentService.registerHorseToTournament(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/advance/{raceId}")
    public ResponseEntity<?> advanceTournament(@PathVariable String raceId) {
        try {
            String result = tournamentService.advanceTournament(raceId);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTournament(@PathVariable String id) {
        try {
            tournamentService.deleteTournament(id);
            return ResponseEntity.ok(Map.of("message", "Tournament deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}