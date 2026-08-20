package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.entity.Horse;
import com.example.horse_racing_management.repository.HorseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/horses") 
@CrossOrigin(origins = "*")
public class HorseController {

    @Autowired
    private HorseRepository horseRepository;

    //POST http://localhost:8080/api/horses
    @PostMapping
    public Horse createHorse(@RequestBody Horse horse) {
        System.out.println("Body: " + horse);
        return horseRepository.save(horse);
    }

    //GET http://localhost:8080/api/horses
    @GetMapping
    public List<Horse> getAllHorses() {
        return horseRepository.findAll();
    }
    @GetMapping("/owner/{ownerId}")
    public List<Horse> getHorsesByOwner(@PathVariable String ownerId) {
        return horseRepository.findByOwnerId(ownerId);
    }

    //PUT http://localhost:8080/api/v1/horses/{id} - sửa thông tin ngựa đã có
    @PutMapping("/{id}")
    public ResponseEntity<Horse> updateHorse(@PathVariable String id, @RequestBody Horse horseDetails) {
        Optional<Horse> existingHorse = horseRepository.findById(id);
        if (existingHorse.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Horse horse = existingHorse.get();
        horse.setName(horseDetails.getName());
        horse.setAge(horseDetails.getAge());
        horse.setBreed(horseDetails.getBreed());
        // Không cho đổi ownerId qua API sửa - giữ nguyên chủ sở hữu gốc

        return ResponseEntity.ok(horseRepository.save(horse));
    }
}