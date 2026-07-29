package com.example.horse_racing_management.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionTest implements CommandLineRunner {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${spring.data.mongodb.database:CHUA_DOC_DUOC}")
    private String configuredDatabase;

    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("---DEBUG ---");
            System.out.println("DB Spring doc duoc: " + configuredDatabase);
            System.out.println("DB thuc te: " + mongoTemplate.getDb().getName());

            if (configuredDatabase.equals(mongoTemplate.getDb().getName())) {
                System.out.println("KET NOI DUNG DATABASE!");
            } else {
                System.out.println("SAI DATABASE! Config: " + configuredDatabase + " | Thuc te: " + mongoTemplate.getDb().getName());
            }
        } catch (Exception e) {
            System.err.println("KET NOI THAT BAI!");
            e.printStackTrace();
        }
    }
}