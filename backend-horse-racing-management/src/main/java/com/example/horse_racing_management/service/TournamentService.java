package com.example.horse_racing_management.service;

import java.util.List;

import com.example.horse_racing_management.dto.RegisterTournamentDTO;
import com.example.horse_racing_management.dto.TournamentDTO;
import com.example.horse_racing_management.entity.Registration;

public interface TournamentService {
    List<TournamentDTO> getAllTournaments();

    TournamentDTO getTournamentById(String id);

    TournamentDTO createTournament(TournamentDTO tournamentDTO);

    TournamentDTO updateTournament(String id, TournamentDTO tournamentDTO);

    void deleteTournament(String id);

    Registration registerHorseToTournament(RegisterTournamentDTO dto);
    
    String advanceTournament(String raceId);
}