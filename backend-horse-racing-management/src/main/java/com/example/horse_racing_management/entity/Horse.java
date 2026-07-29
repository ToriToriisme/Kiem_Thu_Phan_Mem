package com.example.horse_racing_management.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "horses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Horse {

    @Id
    private String id;

    private String name;

    private int age;

    private String breed;

    @Field("owner_id")
    private String ownerId;
}