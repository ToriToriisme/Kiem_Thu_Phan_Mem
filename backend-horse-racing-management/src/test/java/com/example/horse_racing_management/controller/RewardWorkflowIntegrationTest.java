package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.entity.Bet;
import com.example.horse_racing_management.entity.Horse;
import com.example.horse_racing_management.entity.Jockey;
import com.example.horse_racing_management.entity.Race;
import com.example.horse_racing_management.entity.RaceResult;
import com.example.horse_racing_management.entity.User;
import com.example.horse_racing_management.entity.enums.BetStatus;
import com.example.horse_racing_management.entity.enums.RaceStatus;
import com.example.horse_racing_management.repository.BetRepository;
import com.example.horse_racing_management.repository.HorseRepository;
import com.example.horse_racing_management.repository.JockeyRepository;
import com.example.horse_racing_management.repository.RaceRepository;
import com.example.horse_racing_management.repository.RaceResultRepository;
import com.example.horse_racing_management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RewardWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RaceRepository raceRepository;

    @MockitoBean
    private RaceResultRepository raceResultRepository;

    @MockitoBean
    private BetRepository betRepository;

    @MockitoBean
    private HorseRepository horseRepository;

    @MockitoBean
    private JockeyRepository jockeyRepository;

    @MockitoBean
    private UserRepository userRepository;


    // TC01: Không đăng nhập thì không được chia thưởng
    @Test
    void TC01_calculateRewards_withoutAuthentication_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/rewards/calculate/{raceId}", "race-1")
                )
                .andExpect(status().isUnauthorized());
    }


    // TC02: Race không tồn tại
    @Test
    @WithMockUser(
            username = "referee@example.com",
            roles = "RACE_REFEREE"
    )
    void TC02_calculateRewards_raceNotFound_shouldReturn400()
            throws Exception {

        when(raceRepository.findById("race-not-found"))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        post(
                                "/api/v1/rewards/calculate/{raceId}",
                                "race-not-found"
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(
                        jsonPath("$.error")
                                .value("Race not found: race-not-found")
                );
    }


    // TC03: Chưa nhập kết quả cuộc đua thì chưa được chia thưởng
    @Test
    @WithMockUser(
            username = "referee@example.com",
            roles = "RACE_REFEREE"
    )
    void TC03_calculateRewards_withoutRaceResults_shouldReturn400()
            throws Exception {

        Race race = createRace(
                "race-1",
                RaceStatus.IN_PROGRESS
        );

        when(raceRepository.findById("race-1"))
                .thenReturn(Optional.of(race));

        when(raceResultRepository.findByRaceId("race-1"))
                .thenReturn(List.of());

        mockMvc.perform(
                        post(
                                "/api/v1/rewards/calculate/{raceId}",
                                "race-1"
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(
                        jsonPath("$.error")
                                .value(
                                        "No race results found for this race. Please submit results first."
                                )
                );
    }


    // TC04: Hạng 1 chia 80% cho chủ ngựa và 20% cho nài
    @Test
    @WithMockUser(
            username = "referee@example.com",
            roles = "RACE_REFEREE"
    )
    void TC04_firstPlace_shouldGive80PercentToOwnerAnd20PercentToJockey()
            throws Exception {

        Race race = createRace(
                "race-1",
                RaceStatus.IN_PROGRESS
        );

        RaceResult result = createResult(
                "result-1",
                "race-1",
                "horse-1",
                "jockey-1",
                1
        );

        Horse horse = new Horse(
                "horse-1",
                "Thunder",
                4,
                "Thoroughbred",
                "owner-user-1"
        );

        Jockey jockey = new Jockey(
                "jockey-1",
                "Jockey One",
                "JC-001",
                5,
                4.8,
                "jockey-user-1"
        );

        User owner = createUser(
                "owner-user-1",
                1_000_000.0
        );

        User jockeyUser = createUser(
                "jockey-user-1",
                500_000.0
        );

        when(raceRepository.findById("race-1"))
                .thenReturn(Optional.of(race));

        when(raceResultRepository.findByRaceId("race-1"))
                .thenReturn(List.of(result));

        when(betRepository.findByRaceId("race-1"))
                .thenReturn(List.of());

        when(horseRepository.findById("horse-1"))
                .thenReturn(Optional.of(horse));

        when(jockeyRepository.findById("jockey-1"))
                .thenReturn(Optional.of(jockey));

        when(userRepository.findById("owner-user-1"))
                .thenReturn(Optional.of(owner));

        when(userRepository.findById("jockey-user-1"))
                .thenReturn(Optional.of(jockeyUser));

        mockMvc.perform(
                        post(
                                "/api/v1/rewards/calculate/{raceId}",
                                "race-1"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(
                        jsonPath("$.totalPrizeMoneyDistributed")
                                .value(50_000_000.0)
                );

        // Hạng 1 = 50 triệu
        assertEquals(
                50_000_000.0,
                result.getPrizeMoney()
        );

        // Chủ ngựa ban đầu 1 triệu
        // + 80% của 50 triệu = 40 triệu
        assertEquals(
                41_000_000.0,
                owner.getBalance()
        );

        // Nài ban đầu 500 nghìn
        // + 20% của 50 triệu = 10 triệu
        assertEquals(
                10_500_000.0,
                jockeyUser.getBalance()
        );

        verify(userRepository).save(owner);
        verify(userRepository).save(jockeyUser);
    }


    // TC05: Vé cược thắng -> WON và cộng payout vào ví
    @Test
    @WithMockUser(
            username = "referee@example.com",
            roles = "RACE_REFEREE"
    )
    void TC05_winningBet_shouldMarkWonAndAddPayoutToSpectatorWallet()
            throws Exception {

        Race race = createRace(
                "race-1",
                RaceStatus.IN_PROGRESS
        );

        RaceResult result = createResult(
                "result-1",
                "race-1",
                "horse-1",
                null,
                1
        );

        Bet bet = new Bet(
                "bet-1",
                "spectator-1",
                "race-1",
                "horse-1",
                100_000.0,
                1,
                BetStatus.PENDING,
                0.0
        );

        User spectator = createUser(
                "spectator-1",
                500_000.0
        );

        when(raceRepository.findById("race-1"))
                .thenReturn(Optional.of(race));

        when(raceResultRepository.findByRaceId("race-1"))
                .thenReturn(List.of(result));

        when(betRepository.findByRaceId("race-1"))
                .thenReturn(List.of(bet));

        // Không cần xét chủ ngựa trong testcase này
        when(horseRepository.findById("horse-1"))
                .thenReturn(Optional.empty());

        when(userRepository.findById("spectator-1"))
                .thenReturn(Optional.of(spectator));

        mockMvc.perform(
                        post(
                                "/api/v1/rewards/calculate/{raceId}",
                                "race-1"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.winningBets")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.losingBets")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.totalBetPayouts")
                                .value(300_000.0)
                );

        // Cược 100.000 cho hạng 1
        // odds = 3 -> nhận 300.000
        assertEquals(
                BetStatus.WON,
                bet.getStatus()
        );

        assertEquals(
                300_000.0,
                bet.getPayout()
        );

        // Ví ban đầu 500.000
        // + payout 300.000
        assertEquals(
                800_000.0,
                spectator.getBalance()
        );

        verify(userRepository).save(spectator);
        verify(betRepository).saveAll(List.of(bet));
    }


    // TC06: Vé cược thua -> LOST, payout = 0
    @Test
    @WithMockUser(
            username = "referee@example.com",
            roles = "RACE_REFEREE"
    )
    void TC06_losingBet_shouldMarkLostAndNotAddMoneyToWallet()
            throws Exception {

        Race race = createRace(
                "race-1",
                RaceStatus.IN_PROGRESS
        );

        // Ngựa thực tế về hạng 3
        RaceResult result = createResult(
                "result-1",
                "race-1",
                "horse-1",
                null,
                3
        );

        // Khán giả dự đoán hạng 1
        Bet bet = new Bet(
                "bet-1",
                "spectator-1",
                "race-1",
                "horse-1",
                100_000.0,
                1,
                BetStatus.PENDING,
                0.0
        );

        User spectator = createUser(
                "spectator-1",
                500_000.0
        );

        when(raceRepository.findById("race-1"))
                .thenReturn(Optional.of(race));

        when(raceResultRepository.findByRaceId("race-1"))
                .thenReturn(List.of(result));

        when(betRepository.findByRaceId("race-1"))
                .thenReturn(List.of(bet));

        when(horseRepository.findById("horse-1"))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        post(
                                "/api/v1/rewards/calculate/{raceId}",
                                "race-1"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.winningBets")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.losingBets")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.totalBetPayouts")
                                .value(0.0)
                );

        assertEquals(
                BetStatus.LOST,
                bet.getStatus()
        );

        assertEquals(
                0.0,
                bet.getPayout()
        );

        // Ví không được cộng thêm
        assertEquals(
                500_000.0,
                spectator.getBalance()
        );

        verify(
                userRepository,
                never()
        ).save(spectator);

        verify(
                betRepository
        ).saveAll(List.of(bet));
    }


    // TC07: Full workflow Task 5
    @Test
    @WithMockUser(
            username = "referee@example.com",
            roles = "RACE_REFEREE"
    )
    void TC07_fullWorkflow_shouldProcessPrizeBetAndCloseRace()
            throws Exception {

        Race race = createRace(
                "race-final",
                RaceStatus.IN_PROGRESS
        );

        RaceResult result = createResult(
                "result-1",
                "race-final",
                "horse-1",
                "jockey-1",
                1
        );

        Horse horse = new Horse(
                "horse-1",
                "Thunder",
                4,
                "Thoroughbred",
                "owner-user-1"
        );

        Jockey jockey = new Jockey(
                "jockey-1",
                "Jockey One",
                "JC-001",
                5,
                4.8,
                "jockey-user-1"
        );

        User owner = createUser(
                "owner-user-1",
                0.0
        );

        User jockeyUser = createUser(
                "jockey-user-1",
                0.0
        );

        User spectator = createUser(
                "spectator-1",
                100_000.0
        );

        // Vé thắng
        Bet winningBet = new Bet(
                "bet-win",
                "spectator-1",
                "race-final",
                "horse-1",
                100_000.0,
                1,
                BetStatus.PENDING,
                0.0
        );

        // Vé thua vì horse này không có kết quả
        Bet losingBet = new Bet(
                "bet-lose",
                "spectator-2",
                "race-final",
                "horse-no-result",
                50_000.0,
                1,
                BetStatus.PENDING,
                0.0
        );

        when(raceRepository.findById("race-final"))
                .thenReturn(Optional.of(race));

        when(raceResultRepository.findByRaceId("race-final"))
                .thenReturn(List.of(result));

        when(betRepository.findByRaceId("race-final"))
                .thenReturn(
                        List.of(
                                winningBet,
                                losingBet
                        )
                );

        when(horseRepository.findById("horse-1"))
                .thenReturn(Optional.of(horse));

        when(jockeyRepository.findById("jockey-1"))
                .thenReturn(Optional.of(jockey));

        when(userRepository.findById("owner-user-1"))
                .thenReturn(Optional.of(owner));

        when(userRepository.findById("jockey-user-1"))
                .thenReturn(Optional.of(jockeyUser));

        when(userRepository.findById("spectator-1"))
                .thenReturn(Optional.of(spectator));

        mockMvc.perform(
                        post(
                                "/api/v1/rewards/calculate/{raceId}",
                                "race-final"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("SUCCESS")
                )
                .andExpect(
                        jsonPath("$.raceId")
                                .value("race-final")
                )
                .andExpect(
                        jsonPath("$.totalPrizeMoneyDistributed")
                                .value(50_000_000.0)
                )
                .andExpect(
                        jsonPath("$.totalBetPayouts")
                                .value(300_000.0)
                )
                .andExpect(
                        jsonPath("$.winningBets")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.losingBets")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.totalBetsProcessed")
                                .value(2)
                );

        // Kiểm tra giải thưởng
        assertEquals(
                50_000_000.0,
                result.getPrizeMoney()
        );

        assertEquals(
                40_000_000.0,
                owner.getBalance()
        );

        assertEquals(
                10_000_000.0,
                jockeyUser.getBalance()
        );

        // Kiểm tra vé thắng
        assertEquals(
                BetStatus.WON,
                winningBet.getStatus()
        );

        assertEquals(
                300_000.0,
                winningBet.getPayout()
        );

        assertEquals(
                400_000.0,
                spectator.getBalance()
        );

        // Kiểm tra vé thua
        assertEquals(
                BetStatus.LOST,
                losingBet.getStatus()
        );

        assertEquals(
                0.0,
                losingBet.getPayout()
        );

        // Sau khi chia thưởng race phải đóng
        assertEquals(
                RaceStatus.COMPLETED,
                race.getStatus()
        );

        verify(
                raceResultRepository
        ).saveAll(List.of(result));

        verify(
                betRepository
        ).saveAll(
                List.of(
                        winningBet,
                        losingBet
                )
        );

        verify(
                raceRepository
        ).save(race);
    }


    // TC08: Race đã COMPLETED thì không được chia thưởng lần 2
    @Test
    @WithMockUser(
            username = "referee@example.com",
            roles = "RACE_REFEREE"
    )
    void TC08_calculateRewards_completedRace_shouldRejectSecondCalculation()
            throws Exception {

        Race race = createRace(
                "race-completed",
                RaceStatus.COMPLETED
        );

        when(raceRepository.findById("race-completed"))
                .thenReturn(Optional.of(race));

        mockMvc.perform(
                        post(
                                "/api/v1/rewards/calculate/{raceId}",
                                "race-completed"
                        )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value("ERROR")
                )
                .andExpect(
                        jsonPath("$.error")
                                .value(
                                        "Rewards have already been calculated for this race (Status is already COMPLETED)"
                                )
                );

        // Đảm bảo không có dữ liệu tiền nào bị cộng lần nữa
        verify(
                raceResultRepository,
                never()
        ).saveAll(any());

        verify(
                betRepository,
                never()
        ).saveAll(any());

        verify(
                raceRepository,
                never()
        ).save(any(Race.class));

        verify(
                userRepository,
                never()
        ).save(any(User.class));
    }


    private Race createRace(
            String raceId,
            RaceStatus status
    ) {

        Race race = new Race();

        race.setId(raceId);
        race.setName("Test Race");
        race.setStatus(status);

        return race;
    }


    private RaceResult createResult(
            String resultId,
            String raceId,
            String horseId,
            String jockeyId,
            int position
    ) {

        RaceResult result = new RaceResult();

        result.setId(resultId);
        result.setRaceId(raceId);
        result.setHorseId(horseId);
        result.setJockeyId(jockeyId);
        result.setPosition(position);
        result.setFinishTime(120.0);
        result.setPrizeMoney(0.0);

        return result;
    }


    private User createUser(
            String userId,
            double balance
    ) {

        User user = new User();

        user.setId(userId);
        user.setUsername(userId);
        user.setEmail(userId + "@example.com");
        user.setBalance(balance);
        user.setStatus(true);

        return user;
    }
}