package com.example.horse_racing_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.horse_racing_management.entity.RaceResult;

@Repository
public interface RaceResultRepository extends MongoRepository<RaceResult, String> {
    List<RaceResult> findByRaceId(String raceId);
    Optional<RaceResult> findByRaceIdAndHorseId(String raceId, String horseId);
    List<RaceResult> findByJockeyId(String jockeyId);
    List<RaceResult> findByHorseId(String horseId);
    boolean existsByRaceIdAndHorseId(String raceId, String horseId);
    List<RaceResult> findByRaceIdOrderByPositionAsc(String raceId);
}
