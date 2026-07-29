package com.example.horse_racing_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceResultDTO {
    private String id;
    private String raceId;
    private String raceName;
    private String horseId;
    private String horseName;
    private String jockeyId;
    private String jockeyName;
    private Integer position;
    private Double finishTime;
    private Double prizeMoney;
    private Long version;
}
