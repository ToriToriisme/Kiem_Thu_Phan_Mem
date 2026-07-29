package com.example.horse_racing_management.service;

import java.util.Map;

public interface RewardService {
    /**
     * Calculates the rewards for a completed race.
     * This includes:
     * 1. Prize money for top positions (Horses/Jockeys).
     * 2. Payouts for spectators who placed correct bets.
     * 3. Changing the race status to COMPLETED.
     * 
     * @param raceId The ID of the race
     * @return A map containing summary statistics of the reward distribution
     */
    Map<String, Object> calculateRewards(String raceId);
}
