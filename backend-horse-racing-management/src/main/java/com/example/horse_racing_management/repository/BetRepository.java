package com.example.horse_racing_management.repository;

import com.example.horse_racing_management.entity.Bet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BetRepository extends MongoRepository<Bet, String> {
    List<Bet> findBySpectatorId(String spectatorId);
    List<Bet> findByRaceId(String raceId);
}
