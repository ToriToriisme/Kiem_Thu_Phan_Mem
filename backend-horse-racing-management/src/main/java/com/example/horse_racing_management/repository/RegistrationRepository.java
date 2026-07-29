package com.example.horse_racing_management.repository;

import com.example.horse_racing_management.entity.Registration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegistrationRepository extends MongoRepository<Registration, String> {
    List<Registration> findByHorseId(String horseId);
    List<Registration> findByJockeyId(String jockeyId);
    List<Registration> findByHorseIdIn(List<String> horseIds);
    List<Registration> findByRaceId(String raceId);
    boolean existsByRaceIdAndHorseId(String raceId, String horseId);
}