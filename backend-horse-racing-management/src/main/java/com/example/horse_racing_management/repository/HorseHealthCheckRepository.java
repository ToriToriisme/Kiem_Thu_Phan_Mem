package com.example.horse_racing_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.horse_racing_management.entity.HorseHealthCheck;

@Repository
public interface HorseHealthCheckRepository extends MongoRepository<HorseHealthCheck, String> {
    Optional<HorseHealthCheck> findByHorseIdAndRaceId(String horseId, String raceId);
    List<HorseHealthCheck> findByRaceId(String raceId);
    List<HorseHealthCheck> findByHorseId(String horseId);
}
