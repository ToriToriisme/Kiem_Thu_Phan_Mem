package com.example.horse_racing_management.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.horse_racing_management.entity.RefereeReport;

@Repository
public interface RefereeReportRepository extends MongoRepository<RefereeReport, String> {
    List<RefereeReport> findByRefereeId(String refereeId);
    List<RefereeReport> findByRaceId(String raceId);
    List<RefereeReport> findByRefereeIdAndRaceId(String refereeId, String raceId);
}
