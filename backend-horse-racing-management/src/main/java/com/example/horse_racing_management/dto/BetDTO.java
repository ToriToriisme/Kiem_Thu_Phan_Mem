package com.example.horse_racing_management.dto;

import com.example.horse_racing_management.entity.enums.BetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BetDTO {
    private String id;
    private String spectatorId;
    private String raceId;
    private String horseId;
    private Double amount;
    private Integer predictedPosition;
    private BetStatus status;
    private Double payout;
}
