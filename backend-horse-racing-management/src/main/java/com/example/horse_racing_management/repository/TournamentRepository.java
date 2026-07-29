package com.example.horse_racing_management.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.horse_racing_management.entity.Tournament;

@Repository
public interface TournamentRepository extends MongoRepository<Tournament, String> {
}