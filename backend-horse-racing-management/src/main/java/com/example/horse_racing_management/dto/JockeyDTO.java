package com.example.horse_racing_management.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JockeyDTO {
    private String id;
    private String name;
    private String licenseNumber;
    private int experienceYears;
    private double rating;
    private String userId;
}