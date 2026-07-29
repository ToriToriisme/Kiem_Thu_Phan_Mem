package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.dto.RaceDTO;
import com.example.horse_racing_management.service.RaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/races")
public class RaceController {

    @Autowired
    private RaceService raceService;

    @GetMapping
    public ResponseEntity<List<RaceDTO>> getAllRaces() {
        return ResponseEntity.ok(raceService.getAllRaces());
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<?> getRacesByTournamentId(@PathVariable String tournamentId) {
        try {
            return ResponseEntity.ok(raceService.getRacesByTournamentId(tournamentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RaceDTO> getRaceById(@PathVariable String id) {
        return ResponseEntity.ok(raceService.getRaceById(id));
    }

    @PostMapping
    public ResponseEntity<?> createRace(@RequestBody RaceDTO raceDTO) {
        try {
            return ResponseEntity.ok(raceService.createRace(raceDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRace(@PathVariable String id, @RequestBody RaceDTO raceDTO) {
        try {
            return ResponseEntity.ok(raceService.updateRace(id, raceDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRace(@PathVariable String id) {
        try {
            raceService.deleteRace(id);
            return ResponseEntity.ok(Map.of("message", "Race deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}