package com.example.horse_racing_management.service;

import com.example.horse_racing_management.dto.BetDTO;
import com.example.horse_racing_management.entity.Bet;
import com.example.horse_racing_management.entity.Race;
import com.example.horse_racing_management.entity.User;
import com.example.horse_racing_management.entity.enums.BetStatus;
import com.example.horse_racing_management.entity.enums.RaceStatus;
import com.example.horse_racing_management.repository.BetRepository;
import com.example.horse_racing_management.repository.RaceRepository;
import com.example.horse_racing_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpectatorServiceTest {

    @Mock
    private BetRepository betRepository;

    @Mock
    private RaceRepository raceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SpectatorService spectatorService;

    private BetDTO betDTO;
    private Race race;
    private User user;

    @BeforeEach
    void setUp() {
        betDTO = new BetDTO();
        betDTO.setSpectatorId("user01");
        betDTO.setRaceId("race01");
        betDTO.setHorseId("horse01");
        betDTO.setAmount(50000.0);
        betDTO.setPredictedPosition(1);

        race = new Race();
        race.setStatus(RaceStatus.SCHEDULED);

        user = new User();
        user.setBalance(100000.0);
    }

    // TC01: Đặt cược thành công khi số dư lớn hơn tiền cược
    @Test
    void placeBet_WhenBalanceIsEnough_ShouldPlaceBetSuccessfully() {

        when(raceRepository.findById("race01"))
                .thenReturn(Optional.of(race));

        when(userRepository.findById("user01"))
                .thenReturn(Optional.of(user));

        Bet savedBet = new Bet();
        savedBet.setId("bet01");
        savedBet.setStatus(BetStatus.PENDING);

        when(betRepository.save(any(Bet.class)))
                .thenReturn(savedBet);

        BetDTO result = spectatorService.placeBet(betDTO);

        assertNotNull(result);
        assertEquals("bet01", result.getId());
        assertEquals(BetStatus.PENDING, result.getStatus());

        // 100000 - 50000 = 50000
        assertEquals(50000.0, user.getBalance());

        verify(userRepository).save(user);
        verify(betRepository).save(any(Bet.class));
    }

    // TC02: Không cho phép cược vượt quá số dư
    @Test
    void placeBet_WhenBetAmountExceedsBalance_ShouldThrowException() {

        user.setBalance(30000.0);
        betDTO.setAmount(50000.0);

        when(raceRepository.findById("race01"))
                .thenReturn(Optional.of(race));

        when(userRepository.findById("user01"))
                .thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> spectatorService.placeBet(betDTO));

        assertEquals(
                "Số dư không đủ để thực hiện giao dịch",
                exception.getMessage());

        // Số dư không được thay đổi
        assertEquals(30000.0, user.getBalance());

        verify(userRepository, never()).save(any(User.class));
        verify(betRepository, never()).save(any(Bet.class));
    }

    // TC03: Đặt cược đúng bằng số dư
    @Test
    void placeBet_WhenBetAmountEqualsBalance_ShouldLeaveZeroBalance() {

        user.setBalance(50000.0);
        betDTO.setAmount(50000.0);

        when(raceRepository.findById("race01"))
                .thenReturn(Optional.of(race));

        when(userRepository.findById("user01"))
                .thenReturn(Optional.of(user));

        Bet savedBet = new Bet();
        savedBet.setId("bet02");
        savedBet.setStatus(BetStatus.PENDING);

        when(betRepository.save(any(Bet.class)))
                .thenReturn(savedBet);

        BetDTO result = spectatorService.placeBet(betDTO);

        assertNotNull(result);

        // Cược hết số dư → còn 0
        assertEquals(0.0, user.getBalance());

        verify(userRepository).save(user);
        verify(betRepository).save(any(Bet.class));
    }

    // TC04: Race không tồn tại
    @Test
    void placeBet_WhenRaceNotFound_ShouldThrowException() {

        when(raceRepository.findById("race01"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> spectatorService.placeBet(betDTO));

        assertEquals("Race not found", exception.getMessage());

        verify(userRepository, never()).findById(any());
        verify(betRepository, never()).save(any(Bet.class));
    }

    // TC05: Race không ở trạng thái SCHEDULED
    @Test
    void placeBet_WhenRaceIsNotScheduled_ShouldThrowException() {

        race.setStatus(RaceStatus.IN_PROGRESS);

        when(raceRepository.findById("race01"))
                .thenReturn(Optional.of(race));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> spectatorService.placeBet(betDTO));

        assertTrue(
                exception.getMessage()
                        .contains("Race is not open for betting"));

        verify(userRepository, never()).findById(any());
        verify(betRepository, never()).save(any(Bet.class));
    }

    // TC06: User không tồn tại
    @Test
    void placeBet_WhenUserNotFound_ShouldThrowException() {

        when(raceRepository.findById("race01"))
                .thenReturn(Optional.of(race));

        when(userRepository.findById("user01"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> spectatorService.placeBet(betDTO));

        assertEquals("User not found", exception.getMessage());

        verify(betRepository, never()).save(any(Bet.class));
    }

    // TC07: User có balance = null
    @Test
    void placeBet_WhenBalanceIsNull_ShouldTreatAsZero() {

        user.setBalance(null);

        when(raceRepository.findById("race01"))
                .thenReturn(Optional.of(race));

        when(userRepository.findById("user01"))
                .thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> spectatorService.placeBet(betDTO));

        assertEquals(
                "Số dư không đủ để thực hiện giao dịch",
                exception.getMessage());

        assertEquals(0.0, user.getBalance());

        verify(userRepository, never()).save(any(User.class));
        verify(betRepository, never()).save(any(Bet.class));
    }
}