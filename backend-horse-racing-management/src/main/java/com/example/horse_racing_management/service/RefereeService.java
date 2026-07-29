package com.example.horse_racing_management.service;

import java.util.List;

import com.example.horse_racing_management.dto.HorseHealthCheckDTO;
import com.example.horse_racing_management.dto.HorseParticipantDTO;
import com.example.horse_racing_management.dto.RaceResultDTO;
import com.example.horse_racing_management.dto.RaceTrackingDTO;
import com.example.horse_racing_management.dto.RefereeReportDTO;
import com.example.horse_racing_management.dto.ViolationDTO;

public interface RefereeService {
    
    // Referee Report APIs
    List<com.example.horse_racing_management.dto.RaceDTO> getAssignedRaces(String refereeId);
    RaceTrackingDTO getRaceDetails(String raceId);
    List<HorseParticipantDTO> getHorsesByRace(String raceId);
    
    // Create & Update Report
    RefereeReportDTO createReport(String refereeId, String raceId, String reportText);
    RefereeReportDTO updateReport(String reportId, String reportText, Long version);
    RefereeReportDTO getReportById(String reportId);
    List<RefereeReportDTO> getReportsByReferee(String refereeId);
    List<RefereeReportDTO> getReportsByRace(String raceId);
    
    // Race Results - Tracking during race
    RaceResultDTO createRaceResult(String raceId, String horseId, String jockeyId, Integer position, Double finishTime);
    RaceResultDTO updateRaceResult(String resultId, Integer position, Double finishTime, Double prizeMoney, Long version);
    List<RaceResultDTO> getRaceResults(String raceId);
    RaceResultDTO getRaceResultById(String resultId);
    
    // Violations - Track violations during race
    ViolationDTO recordViolation(String raceId, String horseId, String jockeyId, String violationType, 
                                 String description, Double penalty, String severity, String refereeId);
    ViolationDTO updateViolation(String violationId, String description, Double penalty, String severity, Long version);
    List<ViolationDTO> getViolationsByRace(String raceId);
    List<ViolationDTO> getViolationsByHorse(String horseId);
    ViolationDTO getViolationById(String violationId);
    
    // Horse Health Check APIs
    HorseHealthCheckDTO checkHorseHealth(String horseId, String raceId);
    List<HorseHealthCheckDTO> checkAllHorsesHealth(String raceId);
}
