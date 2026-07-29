package com.example.horse_racing_management.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JockeyScheduleDTO {
    private String registrationId;
    private String raceId;
    private String raceName;
    private String tournamentId;
    private String tournamentName;
    private String tournamentStatus;
    private String horseId;
    private String horseName;
    private String jockeyId;
    private String jockeyName;
    private String status;
    private String adminStatus;
    private String startDate;
    private String endDate;
    private List<RaceResultDTO> raceResults;
}