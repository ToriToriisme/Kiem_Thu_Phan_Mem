package com.example.horse_racing_management.service;

import java.util.List;
import java.util.Optional;

import com.example.horse_racing_management.dto.RaceDTO;
import com.example.horse_racing_management.entity.Race;

public interface RaceService {
    List<RaceDTO> getAllRaces();

    List<RaceDTO> getRacesByTournamentId(String tournamentId);
    
    List<RaceDTO> getRacesByRefereeId(String refereeId);

    RaceDTO getRaceById(String id);
    
    Optional<Race> getRaceEntity(String id);

    RaceDTO createRace(RaceDTO raceDTO);

    RaceDTO updateRace(String id, RaceDTO raceDTO);

    void deleteRace(String id);
}