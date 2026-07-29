package com.example.horse_racing_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorseParticipantDTO {
    private String horseId;
    private String horseName;
    private String breed;
    private int age;
    private String jockeyId;
    private String jockeyName;
    private String jockeyLicense;
    private String ownerId;
    private String ownerName;
    private String registrationStatus;
}
