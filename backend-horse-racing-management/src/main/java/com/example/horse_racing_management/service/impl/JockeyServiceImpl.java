package com.example.horse_racing_management.service.impl;

import com.example.horse_racing_management.dto.JockeyDTO;
import com.example.horse_racing_management.entity.Jockey;
import com.example.horse_racing_management.repository.JockeyRepository;
import com.example.horse_racing_management.service.JockeyService;
import com.example.horse_racing_management.entity.User;
import com.example.horse_racing_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JockeyServiceImpl implements JockeyService {

    @Autowired
    private JockeyRepository jockeyRepository;

    @Autowired
    private UserRepository userRepository;

    private JockeyDTO convertToDTO(Jockey jockey) {
        return new JockeyDTO(jockey.getId(), jockey.getName(), jockey.getLicenseNumber(),
                jockey.getExperienceYears(), jockey.getRating(), jockey.getUserId());
    }

    private Jockey convertToEntity(JockeyDTO dto) {
        return new Jockey(dto.getId(), dto.getName(), dto.getLicenseNumber(),
                dto.getExperienceYears(), dto.getRating(), dto.getUserId());
    }

    @Override
    public List<JockeyDTO> getAllJockeys() {
        // Tự động đồng bộ các User có ROLE_JOCKEY chưa có Jockey profile
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getRole() != null && user.getRole().getKey() != null) {
                System.out.println("User: " + user.getUsername() + ", Role: " + user.getRole().getKey());
                if (user.getRole().getKey().contains("JOCKEY")) {
                    if (!jockeyRepository.findByUserId(user.getId()).isPresent()) {
                        Jockey jockey = new Jockey();
                        jockey.setUserId(user.getId());
                        jockey.setName(user.getFullName());
                        jockey.setLicenseNumber("JC-" + System.currentTimeMillis());
                        jockey.setExperienceYears(0);
                        jockey.setRating(0.0);
                        jockeyRepository.save(jockey);
                        System.out.println("Auto-created Jockey for user: " + user.getUsername());
                    }
                }
            }
        }

        return jockeyRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public JockeyDTO getJockeyById(String id) {
        Jockey jockey = jockeyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jockey not found with id: " + id));
        return convertToDTO(jockey);
    }

    @Override
    public JockeyDTO createJockey(JockeyDTO jockeyDTO) {
        if (jockeyRepository.findByLicenseNumber(jockeyDTO.getLicenseNumber()).isPresent()) {
            throw new RuntimeException("License number already exists");
        }
        Jockey jockey = convertToEntity(jockeyDTO);
        jockey.setId(null);
        return convertToDTO(jockeyRepository.save(jockey));
    }

    @Override
    public JockeyDTO updateJockey(String id, JockeyDTO jockeyDTO) {
        Jockey existing = jockeyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jockey not found"));
        existing.setName(jockeyDTO.getName());
        existing.setLicenseNumber(jockeyDTO.getLicenseNumber());
        existing.setExperienceYears(jockeyDTO.getExperienceYears());
        existing.setRating(jockeyDTO.getRating());
        existing.setUserId(jockeyDTO.getUserId());
        return convertToDTO(jockeyRepository.save(existing));
    }

    @Override
    public void deleteJockey(String id) {
        jockeyRepository.deleteById(id);
    }
}