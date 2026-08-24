package com.example.horse_racing_management.service.impl;

import com.example.horse_racing_management.entity.Horse;
import com.example.horse_racing_management.repository.HorseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HorseServiceImplTest {

        @Mock
        private HorseRepository horseRepository;

        @InjectMocks
        private HorseServiceImpl horseService;

        // TC01: Tuổi nhỏ hơn giới hạn tối thiểu
        @Test
        void createHorse_WhenAgeLessThanMinimum_ShouldThrowException() {

                Horse horse = new Horse();
                horse.setName("Horse Test");
                horse.setAge(1);

                assertThrows(
                                IllegalArgumentException.class,
                                () -> horseService.createHorse(horse));

                verify(horseRepository, never()).save(any(Horse.class));
        }

        // TC02: Tuổi bằng giới hạn tối thiểu = 2
        @Test
        void createHorse_WhenAgeIsMinimum_ShouldCreateSuccessfully() {

                Horse horse = new Horse();
                horse.setName("Horse Test");
                horse.setAge(2);

                when(horseRepository.save(horse)).thenReturn(horse);

                Horse result = horseService.createHorse(horse);

                assertNotNull(result);
                assertEquals(2, result.getAge());

                verify(horseRepository).save(horse);
        }

        // TC03: Tuổi nằm trong khoảng hợp lệ
        @Test
        void createHorse_WhenAgeIsValid_ShouldCreateSuccessfully() {

                Horse horse = new Horse();
                horse.setName("Horse Test");
                horse.setAge(10);

                when(horseRepository.save(horse)).thenReturn(horse);

                Horse result = horseService.createHorse(horse);

                assertNotNull(result);
                assertEquals(10, result.getAge());

                verify(horseRepository).save(horse);
        }

        // TC04: Tuổi bằng giới hạn tối đa = 20
        @Test
        void createHorse_WhenAgeIsMaximum_ShouldCreateSuccessfully() {

                Horse horse = new Horse();
                horse.setName("Horse Test");
                horse.setAge(20);

                when(horseRepository.save(horse)).thenReturn(horse);

                Horse result = horseService.createHorse(horse);

                assertNotNull(result);
                assertEquals(20, result.getAge());

                verify(horseRepository).save(horse);
        }

        // TC05: Tuổi lớn hơn giới hạn tối đa
        @Test
        void createHorse_WhenAgeGreaterThanMaximum_ShouldThrowException() {

                Horse horse = new Horse();
                horse.setName("Horse Test");
                horse.setAge(21);

                assertThrows(
                                IllegalArgumentException.class,
                                () -> horseService.createHorse(horse));

                verify(horseRepository, never()).save(any(Horse.class));
        }
}