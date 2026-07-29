package com.example.horse_racing_management.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "jockeys")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jockey {
    @Id
    private String id;
    private String name;
    @Field("license_number")
    private String licenseNumber;
    @Field("experience_years")
    private int experienceYears;
    private double rating;
    @Field("user_id")
    private String userId;
}