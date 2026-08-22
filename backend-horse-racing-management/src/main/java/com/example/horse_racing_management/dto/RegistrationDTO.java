package com.example.horse_racing_management.dto;

import com.example.horse_racing_management.entity.enums.RegistrationStatus;
import com.example.horse_racing_management.entity.Horse;
import com.example.horse_racing_management.entity.Tournament;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDTO {
    private String id;
    private String raceId;
    private String horseId;
    private String jockeyId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date registrationDate;
    private RegistrationStatus status;
    private RegistrationStatus adminStatus;
    private Horse horse;
    private Tournament tournament;
    private com.example.horse_racing_management.entity.Jockey jockey;
}
