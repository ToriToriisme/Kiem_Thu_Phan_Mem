package com.example.horse_racing_management.service;

import com.example.horse_racing_management.entity.Horse;
import com.example.horse_racing_management.repository.HorseRepository;
import com.example.horse_racing_management.service.impl.HorseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorseServiceImplTest {

        @Mock
        private HorseRepository horseRepository;

        @InjectMocks
        private HorseServiceImpl horseService;

        @Test
        void createHorse_success() {

                Horse horse = new Horse(
                                null,
                                "Lightning",
                                5,
                                "Thoroughbred",
                                "owner01");

                Horse savedHorse = new Horse(
                                "horse01",
                                "Lightning",
                                5,
                                "Thoroughbred",
                                "owner01");

                when(horseRepository.save(any(Horse.class)))
                                .thenReturn(savedHorse);

                Horse result = horseService.createHorse(horse);

                assertNotNull(result);
                assertEquals("horse01", result.getId());
                assertEquals("Lightning", result.getName());
                assertEquals(5, result.getAge());
                assertEquals("Thoroughbred", result.getBreed());
                assertEquals("owner01", result.getOwnerId());

                verify(horseRepository)
                                .save(any(Horse.class));
        }
}

// mvn -Dtest=HorseServiceImplTest test