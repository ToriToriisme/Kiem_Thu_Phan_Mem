package com.example.horse_racing_management.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorseHealthCheckDTO {
    private String horseId;
    private String horseName;
    private String raceId;
    private String status; // HEALTHY, INJURED, EXHAUSTED, UNFIT
    private String notes;
    private String checkedBy; // referee name
    private LocalDateTime checkedAt;
    private Boolean approved; // approved to continue racing
}
