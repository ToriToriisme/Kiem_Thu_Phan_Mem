package com.example.horse_racing_management.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceTrackingDTO {
    private String raceId;
    private String raceName;
    private LocalDateTime startTime;
    private Double distance;
    private String status;
    private String location;
    private List<HorseParticipantDTO> participants;
}
