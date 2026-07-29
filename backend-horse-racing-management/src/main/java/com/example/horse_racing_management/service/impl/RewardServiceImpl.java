package com.example.horse_racing_management.service.impl;

import com.example.horse_racing_management.entity.Bet;
import com.example.horse_racing_management.entity.Race;
import com.example.horse_racing_management.entity.RaceResult;
import com.example.horse_racing_management.entity.User;
import com.example.horse_racing_management.entity.enums.BetStatus;
import com.example.horse_racing_management.entity.enums.RaceStatus;
import com.example.horse_racing_management.repository.BetRepository;
import com.example.horse_racing_management.repository.RaceRepository;
import com.example.horse_racing_management.repository.RaceResultRepository;
import com.example.horse_racing_management.repository.UserRepository;
import com.example.horse_racing_management.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RewardServiceImpl implements RewardService {

    @Autowired
    private RaceResultRepository raceResultRepository;

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.horse_racing_management.repository.HorseRepository horseRepository;

    @Autowired
    private com.example.horse_racing_management.repository.JockeyRepository jockeyRepository;

    @Override
    public Map<String, Object> calculateRewards(String raceId) {
        // 1. Fetch race and check status
        Optional<Race> raceOpt = raceRepository.findById(raceId);
        if (raceOpt.isEmpty()) {
            throw new RuntimeException("Race not found: " + raceId);
        }
        Race race = raceOpt.get();
        
        if (race.getStatus() == RaceStatus.COMPLETED) {
            throw new RuntimeException("Rewards have already been calculated for this race (Status is already COMPLETED)");
        }

        // 2. Process Race Results (Prize Money for Horses/Jockeys)
        List<RaceResult> results = raceResultRepository.findByRaceId(raceId);
        if (results.isEmpty()) {
            throw new RuntimeException("No race results found for this race. Please submit results first.");
        }

        double totalPrizeMoney = 0.0;
        for (RaceResult result : results) {
            double prize = 0.0;
            if (result.getPosition() != null) {
                switch (result.getPosition()) {
                    case 1: prize = 50000000.0; break;
                    case 2: prize = 20000000.0; break;
                    case 3: prize = 10000000.0; break;
                    default: prize = 0.0; break;
                }
            }
            result.setPrizeMoney(prize);
            totalPrizeMoney += prize;

            if (prize > 0) {
                // Phân chia tiền thưởng: Chủ ngựa 80%, Nài ngựa 20%
                double ownerShare = prize * 0.8;
                double jockeyShare = prize * 0.2;

                // Cộng tiền cho Chủ ngựa
                if (result.getHorseId() != null) {
                    horseRepository.findById(result.getHorseId()).ifPresent(horse -> {
                        if (horse.getOwnerId() != null) {
                            userRepository.findById(horse.getOwnerId()).ifPresent(owner -> {
                                if (owner.getBalance() == null) owner.setBalance(0.0);
                                owner.setBalance(owner.getBalance() + ownerShare);
                                userRepository.save(owner);
                            });
                        }
                    });
                }

                // Cộng tiền cho Nài ngựa
                if (result.getJockeyId() != null) {
                    jockeyRepository.findById(result.getJockeyId()).ifPresent(jockey -> {
                        if (jockey.getUserId() != null) {
                            userRepository.findById(jockey.getUserId()).ifPresent(jockeyUser -> {
                                if (jockeyUser.getBalance() == null) jockeyUser.setBalance(0.0);
                                jockeyUser.setBalance(jockeyUser.getBalance() + jockeyShare);
                                userRepository.save(jockeyUser);
                            });
                        }
                    });
                }
            }
        }
        raceResultRepository.saveAll(results);

        // 3. Process Bets (Payouts for Spectators)
        List<Bet> bets = betRepository.findByRaceId(raceId);
        double totalPayouts = 0.0;
        int winnersCount = 0;
        int losersCount = 0;

        for (Bet bet : bets) {
            // Find the actual result for the horse the spectator bet on
            Optional<RaceResult> actualResultOpt = results.stream()
                    .filter(r -> r.getHorseId() != null && r.getHorseId().equals(bet.getHorseId()))
                    .findFirst();

            if (actualResultOpt.isPresent()) {
                RaceResult actualResult = actualResultOpt.get();
                if (actualResult.getPosition() != null && actualResult.getPosition() <= bet.getPredictedPosition()) {
                    // Spectator Won
                    bet.setStatus(BetStatus.WON);
                    
                    // Dynamic Odds based on predicted position
                    double odds = 1.0;
                    if (bet.getPredictedPosition() == 1) odds = 3.0;
                    else if (bet.getPredictedPosition() == 2) odds = 2.0;
                    else if (bet.getPredictedPosition() == 3) odds = 1.5;
                    else odds = 1.1; // Default
                    
                    double payout = bet.getAmount() * odds;
                    bet.setPayout(payout);
                    totalPayouts += payout;
                    winnersCount++;
                    
                    // Add payout to user balance
                    Optional<User> userOpt = userRepository.findById(bet.getSpectatorId());
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        if (user.getBalance() == null) user.setBalance(0.0);
                        user.setBalance(user.getBalance() + payout);
                        userRepository.save(user);
                    }
                } else {
                    // Spectator Lost
                    bet.setStatus(BetStatus.LOST);
                    bet.setPayout(0.0);
                    losersCount++;
                }
            } else {
                // Horse didn't participate or no result was submitted for it
                bet.setStatus(BetStatus.LOST);
                bet.setPayout(0.0);
                losersCount++;
            }
        }
        
        if (!bets.isEmpty()) {
            betRepository.saveAll(bets);
        }

        // 4. Mark race as COMPLETED
        race.setStatus(RaceStatus.COMPLETED);
        raceRepository.save(race);

        // 5. Return summary payload
        Map<String, Object> summary = new HashMap<>();
        summary.put("raceId", raceId);
        summary.put("status", "SUCCESS");
        summary.put("message", "Rewards successfully calculated and distributed.");
        summary.put("totalPrizeMoneyDistributed", totalPrizeMoney);
        summary.put("totalBetPayouts", totalPayouts);
        summary.put("winningBets", winnersCount);
        summary.put("losingBets", losersCount);
        summary.put("totalBetsProcessed", bets.size());

        return summary;
    }
}
