package com.example.horse_racing_management.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "race_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaceResult {

    @Id
    private String id;

    @Field("race_id")
    private String raceId;

    @Field("horse_id")
    private String horseId;

    @Field("jockey_id")
    private String jockeyId;

    private Integer position;

    @Field("finish_time")
    private Double finishTime;

    @Field("prize_money")
    private Double prizeMoney;

    @Version
    private Long version;
}
