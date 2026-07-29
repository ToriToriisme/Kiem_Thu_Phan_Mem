package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Reward operations.
 * Handles triggering the automated reward calculations for a race.
 */
@RestController
@RequestMapping("/api/v1/rewards")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RewardController {

    @Autowired
    private RewardService rewardService;

    /**
     * Trigger the calculation of all rewards (Horse prize money & Spectator bet payouts)
     * for a specific race. This should be called after a referee finalizes all results.
     */
    @PostMapping("/calculate/{raceId}")
    public ResponseEntity<?> calculateRewardsForRace(@PathVariable String raceId) {
        try {
            Map<String, Object> summary = rewardService.calculateRewards(raceId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "ERROR",
                "error", e.getMessage()
            ));
        }
    }
}
