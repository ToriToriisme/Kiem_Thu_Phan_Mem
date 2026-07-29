package com.example.horse_racing_management.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "violations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Violation {

    @Id
    private String id;

    @Field("race_id")
    private String raceId;

    @Field("horse_id")
    private String horseId;

    @Field("jockey_id")
    private String jockeyId;

    @Field("violation_type")
    private String violationType; // FOUL, FALSE_START, EQUIPMENT_ISSUE, etc.

    private String description;

    private Double penalty;

    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @Field("referee_id")
    private String refereeId;

    @Field("recorded_at")
    private LocalDateTime recordedAt = LocalDateTime.now();

    @Version
    private Long version;
}
