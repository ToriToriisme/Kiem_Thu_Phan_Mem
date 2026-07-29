package com.example.horse_racing_management.dto;

import java.time.LocalDateTime;

import com.example.horse_racing_management.entity.enums.RaceStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceDTO {
    private String id;
    private String tournamentId;
    private String tournamentName;
    private String name;
    private LocalDateTime startTime;
    private Double distance;
    private RaceStatus status;
    private String refereeId;
    private String refereeName;
    private Integer advancingCount;
}