package com.example.horse_racing_management.service;

import com.example.horse_racing_management.dto.JockeyDTO;
import java.util.List;

public interface JockeyService {
    List<JockeyDTO> getAllJockeys();
    JockeyDTO getJockeyById(String id);
    JockeyDTO createJockey(JockeyDTO jockeyDTO);
    JockeyDTO updateJockey(String id, JockeyDTO jockeyDTO);
    void deleteJockey(String id);
}