package com.example.horse_racing_management.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.horse_racing_management.entity.Violation;

@Repository
public interface ViolationRepository extends MongoRepository<Violation, String> {
    List<Violation> findByRaceId(String raceId);
    List<Violation> findByRefereeId(String refereeId);
    List<Violation> findByHorseId(String horseId);
    List<Violation> findByJockeyId(String jockeyId);
    List<Violation> findByRaceIdAndHorseId(String raceId, String horseId);
}
