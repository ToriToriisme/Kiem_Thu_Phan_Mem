package com.example.horse_racing_management.service.impl;
import com.example.horse_racing_management.repository.JockeyRepository;
import com.example.horse_racing_management.dto.TournamentDTO;
import com.example.horse_racing_management.entity.Tournament;
import com.example.horse_racing_management.repository.RaceRepository;
import com.example.horse_racing_management.repository.TournamentRepository;
import com.example.horse_racing_management.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.horse_racing_management.dto.RegisterTournamentDTO;
import com.example.horse_racing_management.entity.Registration;
import com.example.horse_racing_management.entity.Horse;
import com.example.horse_racing_management.entity.enums.TournamentStatus;
import com.example.horse_racing_management.entity.enums.RegistrationStatus;
import com.example.horse_racing_management.repository.HorseRepository;
import com.example.horse_racing_management.repository.RegistrationRepository;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import com.example.horse_racing_management.entity.Race;
import com.example.horse_racing_management.entity.RaceResult;
import com.example.horse_racing_management.entity.enums.RaceStatus;
import com.example.horse_racing_management.repository.RaceResultRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TournamentServiceImpl implements TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private HorseRepository horseRepository;

    @Autowired
    private RegistrationRepository registrationRepository;
    @Autowired
    private JockeyRepository jockeyRepository;
    
    @Autowired
    private RaceResultRepository raceResultRepository;

    @Override
    public List<TournamentDTO> getAllTournaments() {
        return tournamentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TournamentDTO getTournamentById(String id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giải đấu với id: " + id));

        return convertToDTO(tournament);
    }

    @Override
    public TournamentDTO createTournament(TournamentDTO tournamentDTO) {
        validateTournament(tournamentDTO);

        Tournament tournament = convertToEntity(tournamentDTO);
        Tournament savedTournament = tournamentRepository.save(tournament);

        return convertToDTO(savedTournament);
    }

    @Override
    public TournamentDTO updateTournament(String id, TournamentDTO tournamentDTO) {
        validateTournament(tournamentDTO);

        Tournament existingTournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giải đấu với id: " + id));

        existingTournament.setName(tournamentDTO.getName());
        existingTournament.setDescription(tournamentDTO.getDescription());
        existingTournament.setStartDate(tournamentDTO.getStartDate());
        existingTournament.setEndDate(tournamentDTO.getEndDate());
        existingTournament.setStatus(tournamentDTO.getStatus());

        Tournament updatedTournament = tournamentRepository.save(existingTournament);

        return convertToDTO(updatedTournament);
    }

    @Override
    public void deleteTournament(String id) {
        if (!tournamentRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy giải đấu với id: " + id);
        }

        if (raceRepository.existsByTournamentId(id)) {
            throw new RuntimeException("Không thể xóa giải đấu vì đang có cuộc đua/lịch thi đấu thuộc giải này.");
        }

        tournamentRepository.deleteById(id);
    }

    @Override
    public Registration registerHorseToTournament(RegisterTournamentDTO dto) {
        Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin giải đấu!"));

        if (tournament.getStatus() != TournamentStatus.UPCOMING && tournament.getStatus() != TournamentStatus.ONGOING) {
            throw new RuntimeException("Giải đấu này đã đóng (đã kết thúc), không thể đăng ký thêm!");
        }

        Horse horse = horseRepository.findById(dto.getHorseId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin con ngựa này!"));

        if (registrationRepository.existsByRaceIdAndHorseId(dto.getTournamentId(), dto.getHorseId())) {
            throw new RuntimeException("Con ngựa này đã được đăng ký tham gia giải đấu này rồi!");
        }

        Registration registration = new Registration();
        registration.setRaceId(dto.getTournamentId());
        registration.setHorseId(dto.getHorseId());
        registration.setJockeyId(dto.getJockeyId());
        registration.setRegistrationDate(new Date());
        registration.setStatus(RegistrationStatus.PENDING);

        return registrationRepository.save(registration);
    }

    @Override
    public String advanceTournament(String raceId) {
        Race race = raceRepository.findById(raceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vòng đua!"));

        Integer advancingCount = race.getAdvancingCount() != null ? race.getAdvancingCount() : 3;

        Tournament tournament = tournamentRepository.findById(race.getTournamentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giải đấu!"));

        if (advancingCount <= 3) {
            // Đây được xem là Vòng Chung Kết
            tournament.setStatus(TournamentStatus.COMPLETED);
            tournamentRepository.save(tournament);
            return "Giải đấu đã kết thúc (Vòng chung kết)!";
        }

        // Lấy danh sách kết quả đua (Top N)
        List<RaceResult> results = raceResultRepository.findByRaceIdOrderByPositionAsc(raceId);
        if (results.isEmpty()) {
            throw new RuntimeException("Vòng đua này chưa có kết quả để xét loại!");
        }

        List<RaceResult> topHorses = results.stream().limit(advancingCount).collect(Collectors.toList());

        // Sinh Vòng Đua Mới (Vòng tiếp theo)
        Race nextRace = new Race();
        nextRace.setTournamentId(tournament.getId());
        nextRace.setName(race.getName() + " - Vòng Tiếp Theo");
        nextRace.setStatus(RaceStatus.SCHEDULED);
        nextRace.setAdvancingCount(3); // Mặc định vòng tiếp theo sẽ lấy 3 người (Chung kết), Admin có thể sửa lại sau.
        // Tạm để trống thời gian xuất phát để admin tự set sau
        
        Race savedNextRace = raceRepository.save(nextRace);

        // Đăng ký top ngựa vào vòng đua mới
        for (RaceResult result : topHorses) {
            Registration registration = new Registration();
            registration.setRaceId(savedNextRace.getId());
            registration.setHorseId(result.getHorseId());
            registration.setJockeyId(result.getJockeyId());
            registration.setRegistrationDate(new Date());
            registration.setStatus(RegistrationStatus.APPROVED);
            registrationRepository.save(registration);
        }

        return "Đã tạo vòng đua mới cho Top " + advancingCount + " ngựa đi tiếp!";
    }

    private void validateTournament(TournamentDTO tournamentDTO) {
        if (tournamentDTO.getName() == null || tournamentDTO.getName().isBlank()) {
            throw new RuntimeException("Tên giải đấu không được để trống.");
        }

        if (tournamentDTO.getStartDate() == null) {
            throw new RuntimeException("Ngày bắt đầu không được để trống.");
        }

        if (tournamentDTO.getEndDate() == null) {
            throw new RuntimeException("Ngày kết thúc không được để trống.");
        }

        if (tournamentDTO.getStartDate().after(tournamentDTO.getEndDate())) {
            throw new RuntimeException("Ngày bắt đầu không được sau ngày kết thúc.");
        }

        if (tournamentDTO.getStatus() == null) {
            throw new RuntimeException("Trạng thái giải đấu không được để trống.");
        }
    }

    private TournamentDTO convertToDTO(Tournament tournament) {
        return new TournamentDTO(
                tournament.getId(),
                tournament.getName(),
                tournament.getDescription(),
                tournament.getStartDate(),
                tournament.getEndDate(),
                tournament.getStatus()
        );
    }

    private Tournament convertToEntity(TournamentDTO tournamentDTO) {
        Tournament tournament = new Tournament();
        tournament.setId(tournamentDTO.getId());
        tournament.setName(tournamentDTO.getName());
        tournament.setDescription(tournamentDTO.getDescription());
        tournament.setStartDate(tournamentDTO.getStartDate());
        tournament.setEndDate(tournamentDTO.getEndDate());
        tournament.setStatus(tournamentDTO.getStatus());

        return tournament;
    }
}