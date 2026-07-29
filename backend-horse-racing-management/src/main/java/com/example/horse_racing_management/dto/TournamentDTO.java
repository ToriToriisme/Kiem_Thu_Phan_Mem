package com.example.horse_racing_management.dto;

import java.util.Date;

import com.example.horse_racing_management.entity.enums.TournamentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentDTO {
    private String id;
    private String name;
    private String description;
    private Date startDate;
    private Date endDate;
    private TournamentStatus status;
}