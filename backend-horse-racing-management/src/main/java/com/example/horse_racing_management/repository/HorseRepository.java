package com.example.horse_racing_management.repository;

import com.example.horse_racing_management.entity.Horse;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface HorseRepository extends MongoRepository<Horse, String> {
    // Trong HorseRepository.java
    // Spring Data sẽ cung cấp sẵn các hàm như:
    // save(), findAll(), findById(), deleteById()... mà không cần viết code!
    List<Horse> findByOwnerId(String ownerId);
}
