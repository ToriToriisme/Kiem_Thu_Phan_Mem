package com.example.horse_racing_management.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.example.horse_racing_management.entity.Role;
import lombok.Data;

@Data
public class UserDTO {
  @Id
    private String id;

    private String username;

    private String email;

    private String role;

    @Field("full_name")
    private String fullName;

    private Double balance = 0.0;
    
    private Boolean status = true;
}
