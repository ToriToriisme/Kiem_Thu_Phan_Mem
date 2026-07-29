package com.example.horse_racing_management.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterTournamentDTO {
    private String tournamentId;
    private String horseId;
    private String jockeyId;
}