package com.example.horse_racing_management.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "horse_health_checks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorseHealthCheck {

    @Id
    private String id;

    @Field("horse_id")
    private String horseId;

    @Field("race_id")
    private String raceId;

    private String status; // HEALTHY, INJURED, EXHAUSTED, UNFIT

    private String notes;

    @Field("checked_by")
    private String checkedBy; // referee id

    @Field("checked_at")
    private LocalDateTime checkedAt = LocalDateTime.now();

    private Boolean approved = true;

    @Version
    private Long version;
}
