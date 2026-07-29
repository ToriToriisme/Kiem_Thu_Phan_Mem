package com.example.horse_racing_management.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefereeReportDTO {
    private String id;
    private String raceId;
    private String raceName;
    private String refereeId;
    private String refereeName;
    private String reportText;
    private LocalDateTime createdAt;
    private Long version;
}
