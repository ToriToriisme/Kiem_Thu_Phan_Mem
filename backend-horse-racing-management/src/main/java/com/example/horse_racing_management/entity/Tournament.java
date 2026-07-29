package com.example.horse_racing_management.entity;

import com.example.horse_racing_management.entity.enums.TournamentStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Document(collection = "tournaments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {

    @Id
    private String id;

    private String name;

    private String description;

    @Field("start_date")
    private Date startDate;

    @Field("end_date")
    private Date endDate;

    private TournamentStatus status;
}
