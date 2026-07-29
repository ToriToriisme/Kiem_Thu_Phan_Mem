package com.example.horse_racing_management.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViolationDTO {
    private String id;
    private String raceId;
    private String horseId;
    private String horseName;
    private String jockeyId;
    private String jockeyName;
    private String violationType; // FOUL, FALSE_START, EQUIPMENT_ISSUE, etc.
    private String description;
    private Double penalty; // lệ phí vi phạm
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String refereeId;
    private String refereeName;
    private LocalDateTime recordedAt;
    private Long version;
}
