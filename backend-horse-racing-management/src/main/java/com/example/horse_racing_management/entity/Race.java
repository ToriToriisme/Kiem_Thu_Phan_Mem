package com.example.horse_racing_management.entity;

import com.example.horse_racing_management.entity.enums.RaceStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Document(collection = "races")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Race {

    @Id
    private String id;

    @Field("tournament_id")
    private String tournamentId;

    private String name;

    @Field("start_time")
    private LocalDateTime startTime;

    private Double distance;

    private RaceStatus status;

    @Field("referee_id")
    private String refereeId;
    
    @Field("advancing_count")
    private Integer advancingCount; // Number of horses advancing to next round (e.g., 5). If 3, it means Final Round.
}
