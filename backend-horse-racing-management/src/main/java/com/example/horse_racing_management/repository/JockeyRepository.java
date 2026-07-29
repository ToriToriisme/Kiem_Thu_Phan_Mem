package com.example.horse_racing_management.repository;

import com.example.horse_racing_management.entity.Jockey;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface JockeyRepository extends MongoRepository<Jockey, String> {
    Optional<Jockey> findByLicenseNumber(String licenseNumber);
    Optional<Jockey> findByUserId(String userId);
}