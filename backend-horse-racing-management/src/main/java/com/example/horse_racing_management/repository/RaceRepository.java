package com.example.horse_racing_management.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.horse_racing_management.entity.Race;

@Repository
public interface RaceRepository extends MongoRepository<Race, String> {
    List<Race> findByTournamentIdOrderByStartTimeAsc(String tournamentId);

    boolean existsByTournamentId(String tournamentId);
    
    List<Race> findByRefereeId(String refereeId);
}
