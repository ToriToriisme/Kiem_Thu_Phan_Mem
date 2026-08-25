package com.example.horse_racing_management.controller;

import com.example.horse_racing_management.dto.JockeyScheduleDTO;
import com.example.horse_racing_management.dto.RegisterTournamentDTO;
import com.example.horse_racing_management.entity.Horse;
import com.example.horse_racing_management.entity.Registration;
import com.example.horse_racing_management.entity.Tournament;
import com.example.horse_racing_management.entity.enums.RegistrationStatus;
import com.example.horse_racing_management.entity.enums.TournamentStatus;
import com.example.horse_racing_management.repository.HorseRepository;
import com.example.horse_racing_management.repository.RegistrationRepository;
import com.example.horse_racing_management.repository.TournamentRepository;
import com.example.horse_racing_management.service.RegistrationService;
import com.example.horse_racing_management.service.TournamentService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TournamentRegistrationWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TournamentService tournamentService;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private RegistrationRepository registrationRepository;

    @MockitoBean
    private HorseRepository horseRepository;

    @MockitoBean
    private TournamentRepository tournamentRepository;

    private static final String TOURNAMENT_ID = "tournament-1";
    private static final String HORSE_ID = "horse-1";
    private static final String JOCKEY_ID = "jockey-1";
    private static final String REGISTRATION_ID = "registration-1";


    // =========================================================
    // TC01 - Chưa đăng nhập thì không được đăng ký giải đấu
    // =========================================================
    @Test
    void registerTournament_withoutAuthentication_shouldReturn401()
            throws Exception {

        mockMvc.perform(post("/api/v1/registrations/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequestJson()))
                .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // TC02 - Đăng ký Ngựa + Nài vào giải đấu thành công
    // =========================================================
    @Test
    @WithMockUser(
            username = "owner@example.com",
            roles = "HORSE_OWNER"
    )
    void registerTournament_withValidData_shouldReturn200AndPending()
            throws Exception {

        Registration registration = createPendingRegistration();

        when(tournamentService.registerHorseToTournament(
                any(RegisterTournamentDTO.class)
        )).thenReturn(registration);

        mockMvc.perform(post("/api/v1/registrations/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(REGISTRATION_ID))
                .andExpect(jsonPath("$.raceId").value(TOURNAMENT_ID))
                .andExpect(jsonPath("$.horseId").value(HORSE_ID))
                .andExpect(jsonPath("$.jockeyId").value(JOCKEY_ID))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }


    // =========================================================
    // TC03 - Tournament không tồn tại
    // =========================================================
    @Test
    @WithMockUser(
            username = "owner@example.com",
            roles = "HORSE_OWNER"
    )
    void registerTournament_whenTournamentNotFound_shouldReturn400()
            throws Exception {

        when(tournamentService.registerHorseToTournament(
                any(RegisterTournamentDTO.class)
        )).thenThrow(
                new RuntimeException(
                        "Không tìm thấy thông tin giải đấu!"
                )
        );

        mockMvc.perform(post("/api/v1/registrations/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequestJson()))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .string("Không tìm thấy thông tin giải đấu!"));
    }


    // =========================================================
    // TC04 - Horse không tồn tại
    // =========================================================
    @Test
    @WithMockUser(
            username = "owner@example.com",
            roles = "HORSE_OWNER"
    )
    void registerTournament_whenHorseNotFound_shouldReturn400()
            throws Exception {

        when(tournamentService.registerHorseToTournament(
                any(RegisterTournamentDTO.class)
        )).thenThrow(
                new RuntimeException(
                        "Không tìm thấy thông tin con ngựa này!"
                )
        );

        mockMvc.perform(post("/api/v1/registrations/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequestJson()))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .string("Không tìm thấy thông tin con ngựa này!"));
    }


    // =========================================================
    // TC05 - Horse đã đăng ký Tournament này
    // =========================================================
    @Test
    @WithMockUser(
            username = "owner@example.com",
            roles = "HORSE_OWNER"
    )
    void registerTournament_whenHorseAlreadyRegistered_shouldReturn400()
            throws Exception {

        when(tournamentService.registerHorseToTournament(
                any(RegisterTournamentDTO.class)
        )).thenThrow(
                new RuntimeException(
                        "Con ngựa này đã được đăng ký tham gia giải đấu này rồi!"
                )
        );

        mockMvc.perform(post("/api/v1/registrations/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequestJson()))
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .string(
                                "Con ngựa này đã được đăng ký tham gia giải đấu này rồi!"
                        ));
    }


    // =========================================================
    // TC06 - Jockey xem danh sách đăng ký/lịch của mình
    // =========================================================
    @Test
    @WithMockUser(
            username = "jockey@example.com",
            roles = "JOCKEY"
    )
    void getJockeySchedule_shouldReturnPendingRegistration()
            throws Exception {

        JockeyScheduleDTO schedule = createJockeySchedule("PENDING");

        when(registrationService.getScheduleByJockeyId(JOCKEY_ID))
                .thenReturn(List.of(schedule));

        mockMvc.perform(get(
                        "/api/v1/registrations/jockey/{jockeyId}/schedule",
                        JOCKEY_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationId")
                        .value(REGISTRATION_ID))
                .andExpect(jsonPath("$[0].tournamentId")
                        .value(TOURNAMENT_ID))
                .andExpect(jsonPath("$[0].horseId")
                        .value(HORSE_ID))
                .andExpect(jsonPath("$[0].jockeyId")
                        .value(JOCKEY_ID))
                .andExpect(jsonPath("$[0].status")
                        .value("PENDING"));
    }


    // =========================================================
    // TC07 - Jockey duyệt Registration thành công
    // =========================================================
    @Test
    @WithMockUser(
            username = "jockey@example.com",
            roles = "JOCKEY"
    )
    void approveRegistrationByJockey_shouldReturn200()
            throws Exception {

        mockMvc.perform(put(
                        "/api/v1/registrations/{registrationId}/approve-by-jockey",
                        REGISTRATION_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Đã chấp nhận lịch trình thành công!"));

        verify(registrationService)
                .approveRegistrationByJockey(REGISTRATION_ID);
    }


    // =========================================================
    // TC08 - Jockey duyệt lại Registration đã APPROVED
    // =========================================================
    @Test
    @WithMockUser(
            username = "jockey@example.com",
            roles = "JOCKEY"
    )
    void approveRegistrationByJockey_whenAlreadyApproved_shouldReturn400()
            throws Exception {

        doThrow(
                new RuntimeException("Registration đã được duyệt")
        ).when(registrationService)
                .approveRegistrationByJockey(REGISTRATION_ID);

        mockMvc.perform(put(
                        "/api/v1/registrations/{registrationId}/approve-by-jockey",
                        REGISTRATION_ID
                ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Lỗi: Registration đã được duyệt"));
    }


    // =========================================================
    // TC09 - Admin xem Registration sau khi Jockey đã duyệt
    // =========================================================
    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void getPendingRegistrations_afterJockeyApproved_shouldReturnRegistration()
            throws Exception {

        Registration registration = createApprovedRegistration();

        Horse horse = createHorse();
        Tournament tournament = createTournament();

        when(registrationRepository.findAll())
                .thenReturn(List.of(registration));

        when(horseRepository.findById(HORSE_ID))
                .thenReturn(Optional.of(horse));

        when(tournamentRepository.findById(TOURNAMENT_ID))
                .thenReturn(Optional.of(tournament));

        mockMvc.perform(get(
                        "/api/admin/management/registrations/pending"
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(REGISTRATION_ID))
                .andExpect(jsonPath("$[0].raceId")
                        .value(TOURNAMENT_ID))
                .andExpect(jsonPath("$[0].horseId")
                        .value(HORSE_ID))
                .andExpect(jsonPath("$[0].jockeyId")
                        .value(JOCKEY_ID))
                .andExpect(jsonPath("$[0].status")
                        .value("APPROVED"));
    }


    // =========================================================
    // TC10 - User không phải ADMIN không được xem pending
    // =========================================================
    @Test
    @WithMockUser(
            username = "jockey@example.com",
            roles = "JOCKEY"
    )
    void getPendingRegistrations_withoutAdminRole_shouldReturn403()
            throws Exception {

        mockMvc.perform(get(
                        "/api/admin/management/registrations/pending"
                ))
                .andExpect(status().isForbidden());
    }


    // =========================================================
    // TC11 - Admin duyệt Registration thành công
    // =========================================================
    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void adminApproveRegistration_shouldReturn200AndApproved()
            throws Exception {

        Registration registration = createApprovedRegistration();

        when(registrationRepository.findById(REGISTRATION_ID))
                .thenReturn(Optional.of(registration));

        when(registrationRepository.save(any(Registration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put(
                        "/api/admin/management/registrations/{id}/approve",
                        REGISTRATION_ID
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Đã duyệt đơn đăng ký thành công!"));

        assertEquals(
                RegistrationStatus.APPROVED,
                registration.getAdminStatus()
        );

        verify(registrationRepository).save(registration);
    }


    // =========================================================
    // TC12 - LUỒNG HOÀN CHỈNH
    // Chủ ngựa đăng ký
    // -> Jockey xem yêu cầu
    // -> Jockey duyệt
    // -> Admin xem đơn
    // -> Admin duyệt
    // =========================================================
    @Test
    void tournamentRegistration_fullWorkflow_shouldSucceed()
            throws Exception {

        Registration registration = createPendingRegistration();

        Horse horse = createHorse();
        Tournament tournament = createTournament();

        JockeyScheduleDTO schedule =
                createJockeySchedule("PENDING");


        // =====================================================
        // Mock bước đăng ký Tournament
        // =====================================================
        when(tournamentService.registerHorseToTournament(
                any(RegisterTournamentDTO.class)
        )).thenReturn(registration);


        // =====================================================
        // Mock lịch của Jockey
        // =====================================================
        when(registrationService.getScheduleByJockeyId(JOCKEY_ID))
                .thenReturn(List.of(schedule));


        // =====================================================
        // Khi Jockey approve thì chuyển PENDING -> APPROVED
        // =====================================================
        doAnswer(invocation -> {
            registration.setStatus(
                    RegistrationStatus.APPROVED
            );
            return null;
        }).when(registrationService)
                .approveRegistrationByJockey(REGISTRATION_ID);


        // =====================================================
        // Admin lấy danh sách Registration đã được Jockey duyệt
        // =====================================================
        when(registrationRepository.findAll())
                .thenReturn(List.of(registration));

        when(horseRepository.findById(HORSE_ID))
                .thenReturn(Optional.of(horse));

        when(tournamentRepository.findById(TOURNAMENT_ID))
                .thenReturn(Optional.of(tournament));


        // =====================================================
        // Admin duyệt Registration
        // =====================================================
        when(registrationRepository.findById(REGISTRATION_ID))
                .thenReturn(Optional.of(registration));

        when(registrationRepository.save(any(Registration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        // =====================================================
        // BƯỚC 1: Chủ ngựa đăng ký Ngựa + Nài vào Tournament
        // =====================================================
        mockMvc.perform(post(
                        "/api/v1/registrations/register"
                )
                        .with(user("owner@example.com")
                                .roles("HORSE_OWNER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(REGISTRATION_ID))
                .andExpect(jsonPath("$.horseId")
                        .value(HORSE_ID))
                .andExpect(jsonPath("$.jockeyId")
                        .value(JOCKEY_ID))
                .andExpect(jsonPath("$.status")
                        .value("PENDING"));

        assertEquals(
                RegistrationStatus.PENDING,
                registration.getStatus()
        );


        // =====================================================
        // BƯỚC 2: Jockey xem yêu cầu được gán
        // =====================================================
        mockMvc.perform(get(
                        "/api/v1/registrations/jockey/{jockeyId}/schedule",
                        JOCKEY_ID
                )
                        .with(user("jockey@example.com")
                                .roles("JOCKEY")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationId")
                        .value(REGISTRATION_ID))
                .andExpect(jsonPath("$[0].horseId")
                        .value(HORSE_ID))
                .andExpect(jsonPath("$[0].status")
                        .value("PENDING"));


        // =====================================================
        // BƯỚC 3: Jockey chấp nhận Registration
        // =====================================================
        mockMvc.perform(put(
                        "/api/v1/registrations/{registrationId}/approve-by-jockey",
                        REGISTRATION_ID
                )
                        .with(user("jockey@example.com")
                                .roles("JOCKEY")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Đã chấp nhận lịch trình thành công!"
                        ));

        assertEquals(
                RegistrationStatus.APPROVED,
                registration.getStatus()
        );


        // =====================================================
        // BƯỚC 4: Admin xem các đơn đã được Jockey duyệt
        // =====================================================
        mockMvc.perform(get(
                        "/api/admin/management/registrations/pending"
                )
                        .with(user("admin@example.com")
                                .roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(REGISTRATION_ID))
                .andExpect(jsonPath("$[0].status")
                        .value("APPROVED"));


        // =====================================================
        // BƯỚC 5: Admin duyệt Registration
        // =====================================================
        mockMvc.perform(put(
                        "/api/admin/management/registrations/{id}/approve",
                        REGISTRATION_ID
                )
                        .with(user("admin@example.com")
                                .roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Đã duyệt đơn đăng ký thành công!"
                        ));


        // =====================================================
        // KẾT QUẢ CUỐI CÙNG
        // Jockey đã APPROVED
        // Admin cũng đã APPROVED
        // =====================================================
        assertEquals(
                RegistrationStatus.APPROVED,
                registration.getStatus()
        );

        assertEquals(
                RegistrationStatus.APPROVED,
                registration.getAdminStatus()
        );
    }


    // =========================================================
    // JSON đăng ký Tournament
    // =========================================================
    private String registerRequestJson() {
        return """
                {
                  "tournamentId": "tournament-1",
                  "horseId": "horse-1",
                  "jockeyId": "jockey-1"
                }
                """;
    }


    // =========================================================
    // Registration ban đầu: PENDING
    // =========================================================
    private Registration createPendingRegistration() {

        Registration registration = new Registration();

        registration.setId(REGISTRATION_ID);
        registration.setRaceId(TOURNAMENT_ID);
        registration.setHorseId(HORSE_ID);
        registration.setJockeyId(JOCKEY_ID);
        registration.setRegistrationDate(new Date());
        registration.setStatus(RegistrationStatus.PENDING);

        return registration;
    }


    // =========================================================
    // Registration sau khi Jockey đã duyệt
    // =========================================================
    private Registration createApprovedRegistration() {

        Registration registration =
                createPendingRegistration();

        registration.setStatus(
                RegistrationStatus.APPROVED
        );

        return registration;
    }


    // =========================================================
    // Dữ liệu lịch của Jockey
    // =========================================================
    private JockeyScheduleDTO createJockeySchedule(String status) {

        JockeyScheduleDTO schedule =
                new JockeyScheduleDTO();

        schedule.setRegistrationId(REGISTRATION_ID);

        schedule.setTournamentId(TOURNAMENT_ID);
        schedule.setTournamentName("Sprint 3 Tournament");
        schedule.setTournamentStatus("UPCOMING");

        schedule.setHorseId(HORSE_ID);
        schedule.setHorseName("Test Horse");

        schedule.setJockeyId(JOCKEY_ID);
        schedule.setJockeyName("Test Jockey");

        schedule.setStatus(status);
        schedule.setAdminStatus("PENDING");

        return schedule;
    }


    // =========================================================
    // Horse giả dùng cho Admin Pending List
    // =========================================================
    private Horse createHorse() {

        Horse horse = new Horse();

        horse.setId(HORSE_ID);
        horse.setName("Test Horse");
        horse.setAge(5);
        horse.setBreed("Thoroughbred");
        horse.setOwnerId("owner-1");

        return horse;
    }


    // =========================================================
    // Tournament giả dùng cho Admin Pending List
    // =========================================================
    private Tournament createTournament() {

        Tournament tournament = new Tournament();

        tournament.setId(TOURNAMENT_ID);
        tournament.setName("Sprint 3 Tournament");
        tournament.setDescription(
                "Tournament for Sprint 3 integration test"
        );
        tournament.setStatus(TournamentStatus.UPCOMING);
        tournament.setStartDate(new Date());
        tournament.setEndDate(new Date());

        return tournament;
    }
}