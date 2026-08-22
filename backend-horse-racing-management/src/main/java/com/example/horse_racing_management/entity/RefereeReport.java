package com.example.horse_racing_management.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "referee_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefereeReport {

    @Id
    private String id;

    @Field("race_id")
    private String raceId;

    @Field("referee_id")
    private String refereeId;

    @Field("report_text")
    private String reportText;

    @Field("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Version
    private Long version;
}
