package com.example.horse_racing_management.entity;

import com.example.horse_racing_management.entity.enums.BetStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "bets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bet {

    @Id
    private String id;

    @Field("spectator_id")
    private String spectatorId;

    @Field("race_id")
    private String raceId;

    @Field("horse_id")
    private String horseId;

    private Double amount;

    @Field("predicted_position")
    private Integer predictedPosition;

    private BetStatus status;

    private Double payout;
}
