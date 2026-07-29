package com.example.horse_racing_management.service;

import java.util.List;

import com.example.horse_racing_management.dto.JockeyDTO;
import com.example.horse_racing_management.dto.JockeyScheduleDTO;

public interface RegistrationService {
    List<JockeyDTO> getJockeysByHorseId(String horseId);
    void assignJockeyToRegistration(String registrationId, String jockeyId);
    List<JockeyScheduleDTO> getScheduleByJockeyId(String jockeyId);
    List<JockeyScheduleDTO> getOwnerRegistrations(String ownerId);
    void approveRegistrationByJockey(String registrationId);
    void rejectRegistrationByJockey(String registrationId);
    List<com.example.horse_racing_management.dto.RegistrationDTO> getRegistrationsByTournamentId(String tournamentId);
}