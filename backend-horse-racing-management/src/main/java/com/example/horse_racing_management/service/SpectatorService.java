package com.example.horse_racing_management.service;

import com.example.horse_racing_management.dto.BetDTO;
import com.example.horse_racing_management.entity.Bet;
import com.example.horse_racing_management.entity.Race;
import com.example.horse_racing_management.entity.User;
import com.example.horse_racing_management.entity.enums.BetStatus;
import com.example.horse_racing_management.entity.enums.RaceStatus;
import com.example.horse_racing_management.repository.BetRepository;
import com.example.horse_racing_management.repository.RaceRepository;
import com.example.horse_racing_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SpectatorService {

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private UserRepository userRepository;

    public BetDTO placeBet(BetDTO betDTO) {
        Optional<Race> optionalRace = raceRepository.findById(betDTO.getRaceId());
        if (optionalRace.isEmpty()) {
            throw new RuntimeException("Race not found");
        }
        
        Race race = optionalRace.get();
        if (race.getStatus() != RaceStatus.SCHEDULED) {
            throw new RuntimeException("Race is not open for betting. Current status: " + race.getStatus());
        }

        User user = userRepository.findById(betDTO.getSpectatorId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getBalance() == null) {
            user.setBalance(0.0);
        }

        if (user.getBalance() < betDTO.getAmount()) {
            throw new RuntimeException("Số dư không đủ để thực hiện giao dịch");
        }

        // Deduct balance
        user.setBalance(user.getBalance() - betDTO.getAmount());
        userRepository.save(user);

        Bet bet = new Bet();
        bet.setSpectatorId(betDTO.getSpectatorId());
        bet.setRaceId(betDTO.getRaceId());
        bet.setHorseId(betDTO.getHorseId());
        bet.setAmount(betDTO.getAmount());
        bet.setPredictedPosition(betDTO.getPredictedPosition());
        bet.setStatus(BetStatus.PENDING);
        
        Bet savedBet = betRepository.save(bet);
        
        betDTO.setId(savedBet.getId());
        betDTO.setStatus(savedBet.getStatus());
        
        return betDTO;
    }

    public User topUpWallet(String spectatorId, Double amount) {
        User user = userRepository.findById(spectatorId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getBalance() == null) user.setBalance(0.0);
        user.setBalance(user.getBalance() + amount);
        return userRepository.save(user);
    }

    public Double getWalletBalance(String spectatorId) {
        User user = userRepository.findById(spectatorId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getBalance() == null ? 0.0 : user.getBalance();
    }

    public List<Race> getLiveAndScheduledRaces() {
        return raceRepository.findAll().stream()
                .filter(race -> race.getStatus() == RaceStatus.IN_PROGRESS || race.getStatus() == RaceStatus.SCHEDULED)
                .collect(Collectors.toList());
    }

    public List<Bet> getBetHistory(String spectatorId) {
        return betRepository.findBySpectatorId(spectatorId);
    }

    public List<Race> getAllRaces() {
        return raceRepository.findAll();
    }
}
