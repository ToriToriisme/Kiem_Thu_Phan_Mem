package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.dto.BetDTO;
import com.example.horse_racing_management.entity.Race;
import com.example.horse_racing_management.service.SpectatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/spectator")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SpectatorController {

    @Autowired
    private SpectatorService spectatorService;

    @PostMapping("/bets")
    public ResponseEntity<?> placeBet(@RequestBody BetDTO betDTO) {
        try {
            BetDTO savedBet = spectatorService.placeBet(betDTO);
            return ResponseEntity.ok(savedBet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/races/live")
    public ResponseEntity<?> getLiveRaces() {
        try {
            List<Race> liveRaces = spectatorService.getLiveAndScheduledRaces();
            return ResponseEntity.ok(liveRaces);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/bets/history/{spectatorId}")
    public ResponseEntity<?> getBetHistory(@PathVariable String spectatorId) {
        try {
            return ResponseEntity.ok(spectatorService.getBetHistory(spectatorId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/wallet/top-up/{spectatorId}")
    public ResponseEntity<?> topUpWallet(@PathVariable String spectatorId, @RequestBody Map<String, Double> body) {
        try {
            Double amount = body.get("amount");
            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Amount must be greater than 0"));
            }
            return ResponseEntity.ok(spectatorService.topUpWallet(spectatorId, amount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/wallet/{spectatorId}")
    public ResponseEntity<?> getWalletBalance(@PathVariable String spectatorId) {
        try {
            return ResponseEntity.ok(Map.of("balance", spectatorService.getWalletBalance(spectatorId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/races")
    public ResponseEntity<?> getAllRaces() {
        try {
            return ResponseEntity.ok(spectatorService.getAllRaces());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
