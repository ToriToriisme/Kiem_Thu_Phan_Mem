package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.dto.JockeyDTO;
import com.example.horse_racing_management.service.JockeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/jockeys")
public class JockeyController {

    @Autowired
    private JockeyService jockeyService;

    @GetMapping
    public ResponseEntity<List<JockeyDTO>> getAllJockeys() {
        return ResponseEntity.ok(jockeyService.getAllJockeys());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JockeyDTO> getJockeyById(@PathVariable String id) {
        return ResponseEntity.ok(jockeyService.getJockeyById(id));
    }

    @PostMapping
    public ResponseEntity<JockeyDTO> createJockey(@RequestBody JockeyDTO jockeyDTO) {
        return new ResponseEntity<>(jockeyService.createJockey(jockeyDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JockeyDTO> updateJockey(@PathVariable String id, @RequestBody JockeyDTO jockeyDTO) {
        return ResponseEntity.ok(jockeyService.updateJockey(id, jockeyDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJockey(@PathVariable String id) {
        jockeyService.deleteJockey(id);
        return ResponseEntity.noContent().build();
    }
}