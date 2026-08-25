package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.dto.RaceDTO;
import com.example.horse_racing_management.dto.RaceResultDTO;
import com.example.horse_racing_management.entity.Race;
import com.example.horse_racing_management.entity.User;
import com.example.horse_racing_management.repository.RaceRepository;
import com.example.horse_racing_management.repository.UserRepository;
import com.example.horse_racing_management.service.RefereeService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RefereeWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RaceRepository raceRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefereeService refereeService;

    private static final String RACE_ID = "race-1";
    private static final String REFEREE_ID = "referee-1";
    private static final String HORSE_ID = "horse-1";
    private static final String JOCKEY_ID = "jockey-1";
    private static final String RESULT_ID = "result-1";


    // =========================================================
    // TC01 - Chưa đăng nhập thì không được phân công trọng tài
    // =========================================================
    @Test
    void assignReferee_withoutAuthentication_shouldReturn401() throws Exception {

        mockMvc.perform(put(
                        "/api/admin/management/races/{raceId}/assign-referee/{refereeId}",
                        RACE_ID,
                        REFEREE_ID
                ))
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // TC02 - Đã đăng nhập nhưng không phải ADMIN
    // =========================================================
    @Test
    @WithMockUser(
            username = "referee@example.com",
            roles = "RACE_REFEREE"
    )
    void assignReferee_withoutAdminRole_shouldReturn403() throws Exception {

        mockMvc.perform(put(
                        "/api/admin/management/races/{raceId}/assign-referee/{refereeId}",
                        RACE_ID,
                        REFEREE_ID
                ))
                .andExpect(status().isForbidden());
    }


    // =========================================================
    // TC03 - Admin phân công trọng tài thành công
    // =========================================================
    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void assignReferee_withAdmin_shouldReturn200AndSaveRace() throws Exception {

        Race race = new Race();
        race.setId(RACE_ID);
        race.setName("Sprint 3 Test Race");

        User referee = new User();
        referee.setId(REFEREE_ID);
        referee.setFullName("Test Referee");

        when(raceRepository.findById(RACE_ID))
                .thenReturn(Optional.of(race));

        when(userRepository.findById(REFEREE_ID))
                .thenReturn(Optional.of(referee));

        when(raceRepository.save(any(Race.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put(
                        "/api/admin/management/races/{raceId}/assign-referee/{refereeId}",
                        RACE_ID,
                        REFEREE_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Đã phân công trọng tài Test Referee thành công!"));

        verify(raceRepository).save(race);

        assertEquals(
                REFEREE_ID,
                race.getRefereeId()
        );
    }


    // =========================================================
    // TC04 - Phân công khi race không tồn tại
    // =========================================================
    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void assignReferee_whenRaceNotFound_shouldReturn400() throws Exception {

        User referee = new User();
        referee.setId(REFEREE_ID);
        referee.setFullName("Test Referee");

        when(raceRepository.findById(RACE_ID))
                .thenReturn(Optional.empty());

        when(userRepository.findById(REFEREE_ID))
                .thenReturn(Optional.of(referee));

        mockMvc.perform(put(
                        "/api/admin/management/races/{raceId}/assign-referee/{refereeId}",
                        RACE_ID,
                        REFEREE_ID
                ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy chặng đua hoặc trọng tài!"));
    }


    // =========================================================
    // TC05 - Trọng tài xem race được phân công
    // =========================================================
    @Test
    @WithMockUser(
            username = "referee@example.com",
            roles = "RACE_REFEREE"
    )
    void getAssignedRaces_shouldReturnRaceAssignedToReferee() throws Exception {

        RaceDTO assignedRace = new RaceDTO();
        assignedRace.setId(RACE_ID);
        assignedRace.setName("Sprint 3 Test Race");
        assignedRace.setRefereeId(REFEREE_ID);
        assignedRace.setRefereeName("Test Referee");

        when(refereeService.getAssignedRaces(REFEREE_ID))
                .thenReturn(List.of(assignedRace));

        mockMvc.perform(get(
                        "/api/referee/{refereeId}/assigned-races",
                        REFEREE_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(RACE_ID))
                .andExpect(jsonPath("$[0].name")
                        .value("Sprint 3 Test Race"))
                .andExpect(jsonPath("$[0].refereeId")
                        .value(REFEREE_ID))
                .andExpect(jsonPath("$[0].refereeName")
                        .value("Test Referee"));
    }


    // =========================================================
    // TC06 - Chưa đăng nhập thì không được nhập kết quả
    // =========================================================
    @Test
    void recordRaceResult_withoutAuthentication_shouldReturn401()
            throws Exception {

        mockMvc.perform(post(
                        "/api/referee/race/{raceId}/result",
                        RACE_ID
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(raceResultRequestJson()))
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // TC07 - Trọng tài nhập kết quả hợp lệ
    // =========================================================
    @Test
    @WithMockUser(
            username = "referee@example.com",
            roles = "RACE_REFEREE"
    )
    void recordRaceResult_withAuthentication_shouldReturn200()
            throws Exception {

        RaceResultDTO result = createRaceResultDTO();

        when(refereeService.createRaceResult(
                RACE_ID,
                HORSE_ID,
                JOCKEY_ID,
                1,
                120.45
        )).thenReturn(result);

        mockMvc.perform(post(
                        "/api/referee/race/{raceId}/result",
                        RACE_ID
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(raceResultRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(RESULT_ID))
                .andExpect(jsonPath("$.raceId").value(RACE_ID))
                .andExpect(jsonPath("$.horseId").value(HORSE_ID))
                .andExpect(jsonPath("$.jockeyId").value(JOCKEY_ID))
                .andExpect(jsonPath("$.position").value(1))
                .andExpect(jsonPath("$.finishTime").value(120.45));

        verify(refereeService).createRaceResult(
                RACE_ID,
                HORSE_ID,
                JOCKEY_ID,
                1,
                120.45
        );
    }


    // =========================================================
    // TC08 - Không cho nhập kết quả trùng cho cùng horse + race
    // =========================================================
    @Test
    @WithMockUser(
            username = "referee@example.com",
            roles = "RACE_REFEREE"
    )
    void recordRaceResult_whenResultAlreadyExists_shouldReturn400()
            throws Exception {

        when(refereeService.createRaceResult(
                RACE_ID,
                HORSE_ID,
                JOCKEY_ID,
                1,
                120.45
        )).thenThrow(
                new RuntimeException(
                        "Race result already exists for this horse in this race"
                )
        );

        mockMvc.perform(post(
                        "/api/referee/race/{raceId}/result",
                        RACE_ID
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(raceResultRequestJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value(
                                "Race result already exists for this horse in this race"
                        ));
    }


    // =========================================================
    // TC09 - Lấy danh sách kết quả sau khi nhập
    // =========================================================
    @Test
    void getRaceResults_shouldReturnRecordedResult()
            throws Exception {

        RaceResultDTO result = createRaceResultDTO();

        when(refereeService.getRaceResults(RACE_ID))
                .thenReturn(List.of(result));

        mockMvc.perform(get(
                        "/api/referee/race/{raceId}/results",
                        RACE_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(RESULT_ID))
                .andExpect(jsonPath("$[0].raceId").value(RACE_ID))
                .andExpect(jsonPath("$[0].horseId").value(HORSE_ID))
                .andExpect(jsonPath("$[0].jockeyId").value(JOCKEY_ID))
                .andExpect(jsonPath("$[0].position").value(1))
                .andExpect(jsonPath("$[0].finishTime").value(120.45));
    }


    // =========================================================
    // TC10 - LUỒNG HOÀN CHỈNH
    // Admin phân công -> Referee xem race
    // -> Referee nhập kết quả -> Kiểm tra kết quả
    // =========================================================
    @Test
    void assignRefereeAndRecordResult_workflow_shouldSucceed()
            throws Exception {

        // ---------- Dữ liệu phân công trọng tài ----------
        Race race = new Race();
        race.setId(RACE_ID);
        race.setName("Sprint 3 Workflow Race");

        User referee = new User();
        referee.setId(REFEREE_ID);
        referee.setFullName("Test Referee");

        when(raceRepository.findById(RACE_ID))
                .thenReturn(Optional.of(race));

        when(userRepository.findById(REFEREE_ID))
                .thenReturn(Optional.of(referee));

        when(raceRepository.save(any(Race.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        // ---------- Dữ liệu race đã được phân công ----------
        RaceDTO assignedRace = new RaceDTO();
        assignedRace.setId(RACE_ID);
        assignedRace.setName("Sprint 3 Workflow Race");
        assignedRace.setRefereeId(REFEREE_ID);
        assignedRace.setRefereeName("Test Referee");

        when(refereeService.getAssignedRaces(REFEREE_ID))
                .thenReturn(List.of(assignedRace));


        // ---------- Dữ liệu kết quả cuộc đua ----------
        RaceResultDTO result = createRaceResultDTO();

        when(refereeService.createRaceResult(
                RACE_ID,
                HORSE_ID,
                JOCKEY_ID,
                1,
                120.45
        )).thenReturn(result);

        when(refereeService.getRaceResults(RACE_ID))
                .thenReturn(List.of(result));


        // =====================================================
        // BƯỚC 1: Admin phân công trọng tài
        // =====================================================
        mockMvc.perform(put(
                        "/api/admin/management/races/{raceId}/assign-referee/{refereeId}",
                        RACE_ID,
                        REFEREE_ID
                )
                        .with(user("admin@example.com")
                                .roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Đã phân công trọng tài Test Referee thành công!"
                        ));

        assertEquals(
                REFEREE_ID,
                race.getRefereeId()
        );


        // =====================================================
        // BƯỚC 2: Trọng tài xem race được phân công
        // =====================================================
        mockMvc.perform(get(
                        "/api/referee/{refereeId}/assigned-races",
                        REFEREE_ID
                )
                        .with(user("referee@example.com")
                                .roles("RACE_REFEREE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(RACE_ID))
                .andExpect(jsonPath("$[0].refereeId")
                        .value(REFEREE_ID));


        // =====================================================
        // BƯỚC 3: Trọng tài nhập kết quả cuộc đua
        // =====================================================
        mockMvc.perform(post(
                        "/api/referee/race/{raceId}/result",
                        RACE_ID
                )
                        .with(user("referee@example.com")
                                .roles("RACE_REFEREE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(raceResultRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(RESULT_ID))
                .andExpect(jsonPath("$.raceId")
                        .value(RACE_ID))
                .andExpect(jsonPath("$.horseId")
                        .value(HORSE_ID))
                .andExpect(jsonPath("$.jockeyId")
                        .value(JOCKEY_ID))
                .andExpect(jsonPath("$.position")
                        .value(1));


        // =====================================================
        // BƯỚC 4: Kiểm tra kết quả vừa nhập
        // =====================================================
        mockMvc.perform(get(
                        "/api/referee/race/{raceId}/results",
                        RACE_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(RESULT_ID))
                .andExpect(jsonPath("$[0].raceId")
                        .value(RACE_ID))
                .andExpect(jsonPath("$[0].horseId")
                        .value(HORSE_ID))
                .andExpect(jsonPath("$[0].position")
                        .value(1));
    }


    // =========================================================
    // Hàm tạo body JSON dùng cho API nhập kết quả
    // =========================================================
    private String raceResultRequestJson() {
        return """
                {
                  "horseId": "horse-1",
                  "jockeyId": "jockey-1",
                  "position": 1,
                  "finishTime": 120.45
                }
                """;
    }


    // =========================================================
    // Hàm tạo RaceResultDTO dùng lại trong các test
    // =========================================================
    private RaceResultDTO createRaceResultDTO() {

        RaceResultDTO result = new RaceResultDTO();

        result.setId(RESULT_ID);
        result.setRaceId(RACE_ID);
        result.setRaceName("Sprint 3 Test Race");

        result.setHorseId(HORSE_ID);
        result.setHorseName("Test Horse");

        result.setJockeyId(JOCKEY_ID);
        result.setJockeyName("Test Jockey");

        result.setPosition(1);
        result.setFinishTime(120.45);
        result.setPrizeMoney(5000.0);
        result.setVersion(0L);

        return result;
    }
}