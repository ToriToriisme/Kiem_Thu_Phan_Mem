# DATA FLOW TESTING — BACKEND HORSE RACING MANAGEMENT

> Báo cáo được xây dựng bằng cách đọc trực tiếp source code trong file `backend-horse-racing-management.zip` do bạn cung cấp. Toàn bộ tên class, method, biến, dòng logic trong báo cáo này đều trích xuất từ source thật (không suy diễn/bịa).

---

## PHẦN 1 – KHẢO SÁT PROJECT

### 1.1 Framework & kiến trúc
- **Framework:** Spring Boot (`spring-boot-starter-parent` v4.0.6), Java 17.
- **Database:** MongoDB (`spring-boot-starter-data-mongodb`) — các entity dùng `@Document`, `@Id`, `@Field`, `@Version` (không phải JPA/SQL).
- **Bảo mật:** Spring Security + JWT (`JwtAuthenticationFilter`, `JwtTokenProvider`, `SecurityConfig`).
- **Kiến trúc:** Controller → Service (interface) → ServiceImpl → Repository (Spring Data MongoDB) → Entity, có tầng DTO riêng để convert qua lại.

### 1.2 Các package chính
```
com.example.horse_racing_management
 ├── controller/      (14 REST controller)
 ├── service/          (interface)
 ├── service/impl/     (implementation - nơi chứa logic nghiệp vụ)
 ├── repository/       (Spring Data MongoDB repository)
 ├── dto/               (19 DTO)
 ├── entity/            (13 entity + entity/enums)
 ├── security/          (JWT filter/provider, UserDetails)
 └── config/            (SecurityConfig, MongoConfig)
```

### 1.3 Controller
`AdminApprovalController, AuthController, HorseController, JockeyController, PermissionController, RaceController, RefereeController, RegistrationController, RewardController, RoleController, SpectatorController, TournamentController, TournamentRegistrationController, UserController`

### 1.4 Service / ServiceImpl
`JockeyService, PermissionService, RaceService, RefereeService, RegistrationService, RewardService, RoleService, SpectatorService, TournamentService, UserService` — mỗi service có 1 `*ServiceImpl` tương ứng (trừ `SpectatorService` không thấy Impl riêng trong danh sách file, dùng chung logic từ service khác).

### 1.5 Repository
`HorseRepository, BetRepository, UserRepository, ViolationRepository, JockeyRepository, RefereeReportRepository, RoleRepository, PermissionRepository, RaceResultRepository, TournamentRepository, RegistrationRepository, HorseHealthCheckRepository, RaceRepository` — toàn bộ là Spring Data MongoDB repository (interface, không có logic).

### 1.6 Entity/Model & Enum
`Race, Role, User, Horse, Jockey, Permission, RefereeReport, Violation, Registration, Bet, HorseHealthCheck, RaceResult, Tournament` và enum `BetStatus, TournamentStatus, RaceStatus, RegistrationStatus`.

### 1.7 Module nghiệp vụ chính
1. Quản lý Giải đấu (Tournament) & đăng ký ngựa vào giải.
2. Quản lý Cuộc đua (Race) trong 1 giải đấu, tiến trình loại trực tiếp (advance).
3. Nghiệp vụ Trọng tài (Referee): báo cáo, kết quả đua, vi phạm, kiểm tra sức khỏe ngựa.
4. Đăng ký/gán Jockey cho ngựa, duyệt lịch trình.
5. Auth/Phân quyền (Role, Permission, User).

### 1.8 Bảng đánh giá mức độ phù hợp cho Data Flow Testing

| STT | Class | Loại | Số method | Mức độ phù hợp DFT | Lý do |
|---|---|---|---|---|---|
| 1 | RefereeServiceImpl | ServiceImpl | 17 | **Rất cao** | 528 dòng — nhiều nhất project; nhiều biến, nhiều nhánh if/else lồng nhau, optimistic locking, fallback logic, vòng lặp cập nhật vị trí đua |
| 2 | TournamentServiceImpl | ServiceImpl | 7 | **Rất cao** | `advanceTournament` có nhiều Definition/Use, vòng lặp tạo Registration mới, nhiều nhánh trạng thái giải đấu |
| 3 | RaceServiceImpl | ServiceImpl | 8 | **Cao** | `validateRace` có chuỗi 6 predicate liên tiếp — lý tưởng để phân tích p-use |
| 4 | RegistrationServiceImpl | ServiceImpl | 8 | **Cao** | `buildScheduleDTO` có 3 điểm rẽ nhánh lồng nhau (race vs tournament fallback, jockey optional) |
| 5 | RewardServiceImpl | ServiceImpl | ~8 | Trung bình | Có xử lý dữ liệu nhưng không nằm trong phạm vi ưu tiên theo yêu cầu |
| 6 | UserServiceImpl/RoleServiceImpl | ServiceImpl | ~9 | Trung bình | Chủ yếu CRUD đơn giản, ít nhánh phức tạp |
| 7 | Controller (*) | Controller | — | Thấp (không chọn) | Hầu hết chỉ gọi service + try/catch, không có xử lý dữ liệu nghiệp vụ |
| 8 | Repository (*) | Interface | — | Không áp dụng | Không có thân method, do Spring Data sinh tự động |

### 1.9 Đề xuất phạm vi DFT
Tập trung vào **4 ServiceImpl ưu tiên theo yêu cầu đề bài**: `RefereeServiceImpl`, `TournamentServiceImpl`, `RaceServiceImpl`, `RegistrationServiceImpl`. Đây cũng chính xác là 4 class có điểm phù hợp DFT cao nhất sau khi khảo sát thực tế — không cần thay đổi ưu tiên.

---

## PHẦN 2 – CHỌN METHOD ĐỂ DATA FLOW TESTING

Đã chọn **9 method** (trong khoảng 5–10 theo yêu cầu), phủ đủ 4 class ưu tiên:

| STT | Class | Method | Lý do chọn |
|---|---|---|---|
| M1 | RefereeServiceImpl | `createRaceResult` | Kiểm tra trùng lặp trước khi tạo, nhiều Definition (result object), gọi Repository 2 lần |
| M2 | RefereeServiceImpl | `updateRaceResult` | Optimistic locking (`version`), vòng lặp `for` cập nhật vị trí các ngựa khác khi 1 ngựa bị loại (position=99) — nhiều biến, nhiều nhánh, có redefinition |
| M3 | RefereeServiceImpl | `getHorsesByRace` | 7 điểm rẽ nhánh lồng nhau (fallback tournamentId, null-check horse/owner/jockey), luồng dữ liệu input→output rõ ràng qua nhiều Repository |
| M4 | RefereeServiceImpl | `checkAllHorsesHealth` | Rẽ nhánh theo dữ liệu rỗng, vòng lặp tạo bản ghi mới, gọi chéo sang `getHorsesByRace` |
| M5 | TournamentServiceImpl | `registerHorseToTournament` | 3 validate liên tiếp (tồn tại giải đấu, trạng thái giải đấu, trùng đăng ký), thay đổi trạng thái dữ liệu |
| M6 | TournamentServiceImpl | `advanceTournament` | Method phức tạp nhất của class: nhiều Definition/Use, 2 nhánh chính, vòng lặp tạo Registration hàng loạt, thay đổi trạng thái Tournament |
| M7 | RaceServiceImpl | `updateRace` (gồm `validateRace`) | Chuỗi 6 predicate-use liên tiếp trên các field của DTO — mẫu hình chuẩn cho p-use |
| M8 | RegistrationServiceImpl | `assignJockeyToRegistration` | Validate trạng thái trước khi gán, có khả năng lỗi nghiệp vụ rõ ràng |
| M9 | RegistrationServiceImpl | `buildScheduleDTO` | Method private nhưng phức tạp nhất class: 3 điểm rẽ nhánh lồng nhau (race/tournament fallback + jockey optional), nhiều biến, dùng lại bởi 2 method public |

*Không chọn các method chỉ đơn thuần CRUD 1 dòng (`getAllTournaments`, `getReportById`, …) vì không có Definition/Use đáng kể — đúng theo yêu cầu "không chọn method chỉ vì nó dài" và ưu tiên số lượng Def/Use thực sự.*

---

## PHẦN 3 – PHÂN TÍCH SOURCE CODE

**Quy ước ký hiệu dùng trong báo cáo (thống nhất trước khi phân tích):**
- **D (Definition):** vị trí biến được gán giá trị (khởi tạo, gán lại, tham số đầu vào).
- **c-use (computational use):** biến được dùng để tính toán, gán cho biến/field khác, làm tham số truyền đi, hoặc trả về.
- **p-use (predicate use):** biến được dùng trong biểu thức điều kiện (`if`, toán tử `? :`, điều kiện vòng lặp) để quyết định luồng điều khiển.

---

### M1 — `RefereeServiceImpl.createRaceResult(raceId, horseId, jockeyId, position, finishTime)`

```java
public RaceResultDTO createRaceResult(String raceId, String horseId, String jockeyId,
                                     Integer position, Double finishTime) {
S1  resultRepository.findByRaceIdAndHorseId(raceId, horseId).ifPresent(existing -> {
S2                                                                         // existing defined
S3      throw new RuntimeException("Race result already exists for this horse in this race");
    });
S4  RaceResult result = new RaceResult();
S5  result.setRaceId(raceId);
S6  result.setHorseId(horseId);
S7  result.setJockeyId(jockeyId);
S8  result.setPosition(position);
S9  result.setFinishTime(finishTime);
S10 result.setPrizeMoney(calculatePrizeMoney(position));
S11 RaceResult saved = resultRepository.save(result);
S12 return convertToDTO(saved);
}
```
(S0 = nhận input `raceId, horseId, jockeyId, position, finishTime`)

**Bảng Definition/Use:**

| Variable | Definition | c-use | p-use |
|---|---|---|---|
| raceId | S0 | S1, S5 | — |
| horseId | S0 | S1, S6 | — |
| jockeyId | S0 | S7 | — |
| position | S0 | S8, S10 | — |
| finishTime | S0 | S9 | — |
| existing | S2 (tham số lambda) | — | S3 (điều kiện `ifPresent` kích hoạt nhánh throw) |
| result | S4 | S5–S10, S11 | — |
| saved | S11 | S12 | — |

---

### M2 — `RefereeServiceImpl.updateRaceResult(resultId, position, finishTime, prizeMoney, version)`

```java
public RaceResultDTO updateRaceResult(String resultId, Integer position, Double finishTime,
                                     Double prizeMoney, Long version) {
S1  RaceResult result = resultRepository.findById(resultId)
        .orElseThrow(() -> new RuntimeException("Race result not found: " + resultId));

S2  if (!result.getVersion().equals(version)) {
S3      throw new OptimisticLockingFailureException("...");
    }

S4  Integer oldPosition = result.getPosition();
S5  result.setPosition(position);
S6  result.setFinishTime(finishTime);
S7  result.setPrizeMoney(prizeMoney);
S8  RaceResult updated = resultRepository.save(result);

S9  if (oldPosition != null && oldPosition != 99 && position == 99) {
S10     List<RaceResult> allResults = resultRepository.findByRaceId(result.getRaceId());
S11     for (RaceResult other : allResults) {
S12         if (!other.getId().equals(resultId) && other.getPosition() != 99 && other.getPosition() > oldPosition) {
S13             other.setPosition(other.getPosition() - 1);
S14             resultRepository.save(other);
            }
        }
    }
S15 return convertToDTO(updated);
}
```

**Bảng Definition/Use:**

| Variable | Definition | c-use | p-use |
|---|---|---|---|
| resultId | S0 | S1 | — |
| position | S0 | S5 | S9 |
| finishTime | S0 | S6 | — |
| prizeMoney | S0 | S7 | — |
| version | S0 | — | S2 |
| result | S1 | S4 (getPosition), S5–S7, S8, S10 (getRaceId) | S2 (getVersion) |
| oldPosition | S4 | — | S9 |
| updated | S8 | S15 | — |
| allResults | S10 | S11 | — |
| other (vòng lặp) | S11 | S13, S14 | S12 |
| other (sau redefine) | S13 | S14 | — |

*Ghi chú DFT quan trọng: `other` bị **redefine tại S13** (position mới) trước khi được `save` ở S14 — đây là 1 DU-path hợp lệ riêng biệt (D(S13)→U(S14)), không được gộp chung với D(S11)→U(S12/S13).*

---

### M3 — `RefereeServiceImpl.getHorsesByRace(raceId)`

```java
public List<HorseParticipantDTO> getHorsesByRace(String raceId) {
S1  List<Registration> registrations = registrationRepository.findByRaceId(raceId);

S2  if (registrations.isEmpty()) {
S3      raceService.getRaceEntity(raceId).ifPresent(race -> {
S4          if (race.getTournamentId() != null && !race.getTournamentId().isBlank()) {
S5              registrations.addAll(registrationRepository.findByRaceId(race.getTournamentId()));
            }
        });
    }

S6  return registrations.stream()
        .map(reg -> {
S7          HorseParticipantDTO dto = new HorseParticipantDTO();
S8          dto.setRegistrationStatus(reg.getStatus().toString());

S9          if (reg.getHorseId() != null) {
S10             Horse horse = horseRepository.findById(reg.getHorseId()).orElse(null);
S11             if (horse != null) {
S12                 dto.setHorseId(horse.getId()); ...
S13                 User owner = userRepository.findById(horse.getOwnerId()).orElse(null);
S14                 if (owner != null) {
S15                     dto.setOwnerName(owner.getFullName());
                    }
                }
            }
S16         if (reg.getJockeyId() != null) {
S17             Jockey jockey = jockeyRepository.findById(reg.getJockeyId()).orElse(null);
S18             if (jockey != null) {
S19                 dto.setJockeyId(jockey.getId()); ...
                }
            }
S20         return dto;
        }).collect(Collectors.toList());
}
```

**Bảng Definition/Use:**

| Variable | Definition | c-use | p-use |
|---|---|---|---|
| raceId | S0 | S1, S3, S5 (gián tiếp qua `race.getTournamentId()`) | — |
| registrations | S1 | S6 | S2 |
| registrations (sau `addAll` S5) | S5 (mutate) | S6 | — |
| race | S3 (tham số lambda) | — | S4 |
| reg | S6 (tham số lambda `map`) | S8, S10, S17 | S9, S16 |
| horse | S10 | S12, S13 | S11 |
| owner | S13 | S15 | S14 |
| jockey | S17 | S19 | S18 |
| dto | S7 | S8, S12, S15, S19 | — |

---

### M4 — `RefereeServiceImpl.checkAllHorsesHealth(raceId)`

```java
public List<HorseHealthCheckDTO> checkAllHorsesHealth(String raceId) {
S1  List<HorseHealthCheck> healthChecks = healthCheckRepository.findByRaceId(raceId);
S2  if (healthChecks.isEmpty()) {
S3      List<HorseParticipantDTO> participants = getHorsesByRace(raceId);
S4      return participants.stream()
            .map(participant -> {
S5              HorseHealthCheck check = new HorseHealthCheck();
S6              check.setHorseId(participant.getHorseId());
S7              check.setRaceId(raceId);
S8              check.setStatus("HEALTHY");
S9              check.setApproved(true);
S10             HorseHealthCheck saved = healthCheckRepository.save(check);
S11             return convertToDTO(saved);
            }).collect(Collectors.toList());
    }
S12 return healthChecks.stream().map(this::convertToDTO).collect(Collectors.toList());
}
```

**Bảng Definition/Use:**

| Variable | Definition | c-use | p-use |
|---|---|---|---|
| raceId | S0 | S3, S7 | — |
| healthChecks | S1 | S12 | S2 |
| participants | S3 | S4 | — |
| participant | S4 (tham số lambda) | S6 | — |
| check | S5 | S6–S9, S10 | — |
| saved | S10 | S11 | — |

---

### M5 — `TournamentServiceImpl.registerHorseToTournament(dto)`

```java
public Registration registerHorseToTournament(RegisterTournamentDTO dto) {
S1  Tournament tournament = tournamentRepository.findById(dto.getTournamentId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin giải đấu!"));

S2  if (tournament.getStatus() != TournamentStatus.UPCOMING && tournament.getStatus() != TournamentStatus.ONGOING) {
S3      throw new RuntimeException("Giải đấu này đã đóng...");
    }

S4  Horse horse = horseRepository.findById(dto.getHorseId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin con ngựa này!"));

S5  if (registrationRepository.existsByRaceIdAndHorseId(dto.getTournamentId(), dto.getHorseId())) {
S6      throw new RuntimeException("Con ngựa này đã được đăng ký...");
    }

S7  Registration registration = new Registration();
S8  registration.setRaceId(dto.getTournamentId());
S9  registration.setHorseId(dto.getHorseId());
S10 registration.setJockeyId(dto.getJockeyId());
S11 registration.setRegistrationDate(new Date());
S12 registration.setStatus(RegistrationStatus.PENDING);
S13 return registrationRepository.save(registration);
}
```

**Bảng Definition/Use:**

| Variable | Definition | c-use | p-use |
|---|---|---|---|
| dto | S0 (tham số) | S1, S4, S5, S8, S9, S10 | — |
| tournament | S1 | — | S2 |
| horse | S4 | **(không có use nào sau đó)** | — |
| registration | S7 | S8–S12, S13 | — |

⚠️ **Phát hiện DFT:** biến `horse` được **Definition tại S4 nhưng không có Use** nào ở phía sau — object `horse` chỉ được lấy ra để kiểm tra tồn tại (do `orElseThrow`) rồi bị bỏ. Đây là anomaly loại **"define without use"**, ghi nhận chi tiết ở Phần 12.

---

### M6 — `TournamentServiceImpl.advanceTournament(raceId)`

```java
public String advanceTournament(String raceId) {
S1  Race race = raceRepository.findById(raceId).orElseThrow(() -> new RuntimeException("Không tìm thấy vòng đua!"));
S2  Integer advancingCount = race.getAdvancingCount() != null ? race.getAdvancingCount() : 3;
S3  Tournament tournament = tournamentRepository.findById(race.getTournamentId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy giải đấu!"));

S4  if (advancingCount <= 3) {
S5      tournament.setStatus(TournamentStatus.COMPLETED);
S6      tournamentRepository.save(tournament);
S7      return "Giải đấu đã kết thúc (Vòng chung kết)!";
    }

S8  List<RaceResult> results = raceResultRepository.findByRaceIdOrderByPositionAsc(raceId);
S9  if (results.isEmpty()) {
S10     throw new RuntimeException("Vòng đua này chưa có kết quả để xét loại!");
    }
S11 List<RaceResult> topHorses = results.stream().limit(advancingCount).collect(Collectors.toList());

S12 Race nextRace = new Race();
S13 nextRace.setTournamentId(tournament.getId());
S14 nextRace.setName(race.getName() + " - Vòng Tiếp Theo");
S15 nextRace.setStatus(RaceStatus.SCHEDULED);
S16 nextRace.setAdvancingCount(3);
S17 Race savedNextRace = raceRepository.save(nextRace);

S18 for (RaceResult result : topHorses) {
S19     Registration registration = new Registration();
S20     registration.setRaceId(savedNextRace.getId());
S21     registration.setHorseId(result.getHorseId());
S22     registration.setJockeyId(result.getJockeyId());
S23     registration.setRegistrationDate(new Date());
S24     registration.setStatus(RegistrationStatus.APPROVED);
S25     registrationRepository.save(registration);
    }
S26 return "Đã tạo vòng đua mới cho Top " + advancingCount + " ngựa đi tiếp!";
}
```

**Bảng Definition/Use:**

| Variable | Definition | c-use | p-use |
|---|---|---|---|
| raceId | S0 | S1, S8 | — |
| race | S1 | S2 (getAdvancingCount), S3 (getTournamentId), S14 (getName) | — |
| advancingCount | S2 | S11, S26 | S4 |
| tournament | S3 | S5, S6, S13 (getId) | — |
| results | S8 | S11 | S9 |
| topHorses | S11 | S18 | — |
| nextRace | S12 | S13–S16, S17 | — |
| savedNextRace | S17 | S20 | — |
| result (vòng lặp) | S18 | S21, S22 | — |
| registration (vòng lặp) | S19 | S20–S24, S25 | — |

---

### M7 — `RaceServiceImpl.updateRace(id, raceDTO)` (bao gồm `validateRace`)

```java
public RaceDTO updateRace(String id, RaceDTO raceDTO) {
V1  if (raceDTO.getTournamentId() == null || raceDTO.getTournamentId().isBlank()) throw ...;
V2  if (!tournamentRepository.existsById(raceDTO.getTournamentId())) throw ...;
V3  if (raceDTO.getName() == null || raceDTO.getName().isBlank()) throw ...;
V4  if (raceDTO.getStartTime() == null) throw ...;
V5  if (raceDTO.getDistance() == null || raceDTO.getDistance() <= 0) throw ...;
V6  if (raceDTO.getStatus() == null) throw ...;

U1  Race existingRace = raceRepository.findById(id).orElseThrow(() -> new RuntimeException("..."));
U2  existingRace.setTournamentId(raceDTO.getTournamentId());
U3  existingRace.setName(raceDTO.getName());
U4  existingRace.setStartTime(raceDTO.getStartTime());
U5  existingRace.setDistance(raceDTO.getDistance());
U6  existingRace.setStatus(raceDTO.getStatus());
U7  existingRace.setRefereeId(raceDTO.getRefereeId());
U8  Race updatedRace = raceRepository.save(existingRace);
U9  return convertToDTO(updatedRace);
}
```

**Bảng Definition/Use:**

| Variable | Definition | c-use | p-use |
|---|---|---|---|
| id | S0 | U1 | — |
| raceDTO.tournamentId | S0 | U2 | V1, V2 |
| raceDTO.name | S0 | U3 | V3 |
| raceDTO.startTime | S0 | U4 | V4 |
| raceDTO.distance | S0 | U5 | V5 |
| raceDTO.status | S0 | U6 | V6 |
| raceDTO.refereeId | S0 | U7 | — |
| existingRace | U1 | U2–U7, U8 | — |
| updatedRace | U8 | U9 | — |

---

### M8 — `RegistrationServiceImpl.assignJockeyToRegistration(registrationId, jockeyId)`

```java
public void assignJockeyToRegistration(String registrationId, String jockeyId) {
S1  Registration registration = registrationRepository.findById(registrationId)
        .orElseThrow(() -> new RuntimeException("Registration not found"));
S2  if (registration.getJockeyId() != null && !registration.getJockeyId().isEmpty()) {
S3      throw new RuntimeException("Registration đã có Jockey, không thể gán lại.");
    }
S4  jockeyRepository.findById(jockeyId).orElseThrow(() -> new RuntimeException("Jockey not found"));
S5  registration.setJockeyId(jockeyId);
S6  registrationRepository.save(registration);
}
```

**Bảng Definition/Use:**

| Variable | Definition | c-use | p-use |
|---|---|---|---|
| registrationId | S0 | S1 | — |
| jockeyId | S0 | S4, S5 | — |
| registration | S1 | S5, S6 | S2 |

---

### M9 — `RegistrationServiceImpl.buildScheduleDTO(reg)` (private, dùng bởi `getScheduleByJockeyId` & `getOwnerRegistrations`)

```java
private JockeyScheduleDTO buildScheduleDTO(Registration reg) {
S1  JockeyScheduleDTO dto = new JockeyScheduleDTO();
S2  dto.setRegistrationId(reg.getId());
S3  dto.setStatus(reg.getStatus() != null ? reg.getStatus().name() : "PENDING");
S4  dto.setAdminStatus(reg.getAdminStatus() != null ? reg.getAdminStatus().name() : "PENDING");

S5  String registrationRef = reg.getRaceId();
S6  Race race = raceRepository.findById(registrationRef).orElse(null);
S7  if (race != null) {
S8      dto.setRaceId(race.getId());
S9      dto.setRaceName(race.getName());
S10     if (race.getTournamentId() != null && !race.getTournamentId().isEmpty()) {
S11         Tournament tournament = tournamentRepository.findById(race.getTournamentId())
                .orElseThrow(() -> new RuntimeException("Tournament not found for id: " + race.getTournamentId()));
S12-16      dto.setTournamentId/Name/Status/StartDate/EndDate(tournament...);
        }
S17     dto.setRaceResults(refereeService.getRaceResults(race.getId()));
    } else {
S18     Tournament tournament2 = tournamentRepository.findById(registrationRef)
            .orElseThrow(() -> new RuntimeException("Tournament not found for id: " + registrationRef));
S19-23  dto.setTournamentId/Name/Status/StartDate/EndDate(tournament2...);
S24     dto.setRaceResults(getRaceResultsForTournament(tournament2.getId()));
    }

S25 Horse horse = horseRepository.findById(reg.getHorseId())
        .orElseThrow(() -> new RuntimeException("Horse not found for id: " + reg.getHorseId()));
S26 dto.setHorseId(horse.getId());
S27 dto.setHorseName(horse.getName());

S28 if (reg.getJockeyId() != null && !reg.getJockeyId().isEmpty()) {
S29     dto.setJockeyId(reg.getJockeyId());
S30     jockeyRepository.findById(reg.getJockeyId()).ifPresent(jockey -> dto.setJockeyName(jockey.getName()));
    }
S31 return dto;
}
```

**Bảng Definition/Use:**

| Variable | Definition | c-use | p-use |
|---|---|---|---|
| reg | S0 (tham số) | S2, S5, S25, S29, S30 | S3, S4, S7 (gián tiếp qua S6/registrationRef), S10, S28 |
| dto | S1 | S2–S4, S8–S9, S12–S17 hoặc S19–S24, S26–S27, S29–S30 | — |
| registrationRef | S5 | S6, S18 | — |
| race | S6 | S8, S9, S17 (getId) | S7, S10 |
| tournament (S11) | S11 | S12–S16 | — |
| tournament2 (S18) | S18 | S19–S24 | — |
| horse | S25 | S26, S27 | — |

---

## PHẦN 4 – CONTROL FLOW GRAPH

*(CFG chỉ vẽ cho các method có rẽ nhánh/vòng lặp; M1, M8 đơn giản chỉ 1 nhánh → mô tả bằng chữ là đủ.)*

**M1 – createRaceResult** (1 nhánh thoát sớm):
```
S1(findByRaceIdAndHorseId)
 ├── existing present → S3 throw (kết thúc)
 └── absent → S4→S5→S6→S7→S8→S9→S10→S11→S12
```

**M2 – updateRaceResult** (2 nhánh, 1 vòng lặp lồng 1 nhánh):
```
S1 → S2
S2 ── true(version mismatch) → S3 throw (kết thúc)
S2 ── false → S4→S5→S6→S7→S8→S9
S9 ── false → S15 (kết thúc)
S9 ── true  → S10→S11(loop start)
        S11 → S12
        S12 ── false → (loop tiếp/kết thúc loop) → S15
        S12 ── true  → S13→S14 → (loop tiếp/kết thúc loop) → S15
```

**M3 – getHorsesByRace** (Branch A→B lồng, rồi C→D→E, F→G độc lập, lặp qua `reg`):
```
S1 → S2
S2 ── true  → S3(getRaceEntity) → S4
        S4 ── true  → S5(addAll)
        S4 ── false → bỏ qua
S2 ── false → bỏ qua
→ S6 (stream map cho từng reg):
   S7→S8→S9
   S9──true→S10→S11
        S11──true→S12..S13→S14
             S14──true→S15
        S11──false→bỏ qua
   S9──false→bỏ qua
   →S16
   S16──true→S17→S18
        S18──true→S19..
        S18──false→bỏ qua
   S16──false→bỏ qua
   →S20(return dto)
```

**M4 – checkAllHorsesHealth** (1 nhánh chính, có vòng lặp bên trong nhánh True):
```
S1 → S2
S2 ── true  → S3 → S4(loop map: S5..S10→S11) → kết thúc list
S2 ── false → S12 → kết thúc
```

**M5 – registerHorseToTournament** (2 nhánh thoát sớm liên tiếp):
```
S1 → S2
S2 ── true(status đóng) → S3 throw (kết thúc)
S2 ── false → S4 → S5
S5 ── true(đã đăng ký) → S6 throw (kết thúc)
S5 ── false → S7→...→S13 (kết thúc bình thường)
```

**M6 – advanceTournament** (2 nhánh chính, 1 nhánh phụ throw, 1 vòng lặp):
```
S1→S2→S3→S4
S4 ── true(vòng chung kết) → S5→S6→S7 (kết thúc)
S4 ── false → S8→S9
        S9 ── true(chưa có kết quả) → S10 throw (kết thúc)
        S9 ── false → S11→S12→...→S17→S18(loop qua topHorses: S19..S25)→S26 (kết thúc)
```

**M7 – updateRace/validateRace** (chuỗi 6 predicate tuần tự — mỗi cái có thể thoát sớm):
```
V1──true→throw   V1──false→V2
V2──true→throw   V2──false→V3
V3──true→throw   V3──false→V4
V4──true→throw   V4──false→V5
V5──true→throw   V5──false→V6
V6──true→throw   V6──false→U1→U2→...→U8→U9
```

**M9 – buildScheduleDTO** (Branch A [race≠null] lồng Branch B [tournamentId], độc lập với Branch C [jockey]):
```
S1→S2→S3→S4→S5→S6→S7
S7 ── true(race≠null) → S8→S9→S10
        S10 ── true  → S11→S12..S16
        S10 ── false → bỏ qua
     → S17
S7 ── false → S18→S19..S24
→ S25→S26→S27→S28
S28 ── true → S29→S30
S28 ── false → bỏ qua
→ S31 (return)
```
Số đường đi khả thi qua M9: khi Branch A = true có 2×2 = 4 tổ hợp (B × C); khi Branch A = false có 1×2 = 2 tổ hợp (C) → **tổng 6 đường đi khả thi** (đủ nhỏ để bàn All DU-Paths ở Phần 6).

---

## PHẦN 5 – DU PAIR / DU CHAIN

*Ký hiệu DU ID theo dạng `<Mã method>-DUn`. Chỉ những cặp D→U có ý nghĩa kiểm thử (không liệt kê lại các setter đơn thuần không có nhánh) được đưa vào bảng để giữ báo cáo có thể theo dõi được — phương pháp này được nêu rõ để không "bịa" dữ liệu nhưng vẫn khả thi khi trình bày.*

### M1 – createRaceResult (11 DU pairs)
| DU ID | Variable | Definition | Use | Type |
|---|---|---|---|---|
| M1-DU1 | raceId | S0 | S1 | c-use |
| M1-DU2 | raceId | S0 | S5 | c-use |
| M1-DU3 | horseId | S0 | S1 | c-use |
| M1-DU4 | horseId | S0 | S6 | c-use |
| M1-DU5 | jockeyId | S0 | S7 | c-use |
| M1-DU6 | position | S0 | S8 | c-use |
| M1-DU7 | position | S0 | S10 | c-use |
| M1-DU8 | finishTime | S0 | S9 | c-use |
| M1-DU9 | existing | S2 | S3 | p-use |
| M1-DU10 | result | S4 | S11 | c-use |
| M1-DU11 | saved | S11 | S12 | c-use |

### M2 – updateRaceResult (16 DU pairs)
| DU ID | Variable | Definition | Use | Type |
|---|---|---|---|---|
| M2-DU1 | resultId | S0 | S1 | c-use |
| M2-DU2 | version | S0 | S2 | p-use |
| M2-DU3 | result | S1 | S2 | p-use |
| M2-DU4 | result | S1 | S4 | c-use |
| M2-DU5 | result | S1 | S8 | c-use |
| M2-DU6 | result | S1 | S10 | c-use |
| M2-DU7 | position | S0 | S5 | c-use |
| M2-DU8 | position | S0 | S9 | p-use |
| M2-DU9 | finishTime | S0 | S6 | c-use |
| M2-DU10 | prizeMoney | S0 | S7 | c-use |
| M2-DU11 | oldPosition | S4 | S9 | p-use |
| M2-DU12 | updated | S8 | S15 | c-use |
| M2-DU13 | allResults | S10 | S11 | c-use |
| M2-DU14 | other | S11 | S12 | p-use |
| M2-DU15 | other | S11 | S13 | c-use |
| M2-DU16 | other (redefine) | S13 | S14 | c-use |

### M3 – getHorsesByRace (16 DU pairs)
| DU ID | Variable | Definition | Use | Type |
|---|---|---|---|---|
| M3-DU1 | raceId | S0 | S1 | c-use |
| M3-DU2 | raceId | S0 | S3 | c-use |
| M3-DU3 | registrations | S1 | S2 | p-use |
| M3-DU4 | registrations | S1 | S6 | c-use |
| M3-DU5 | registrations (sau addAll S5) | S5 | S6 | c-use |
| M3-DU6 | race | S3 | S4 | p-use |
| M3-DU7 | reg | S6 | S8 | p-use |
| M3-DU8 | reg | S6 | S9 | p-use |
| M3-DU9 | reg | S6 | S16 | p-use |
| M3-DU10 | horse | S10 | S11 | p-use |
| M3-DU11 | horse | S10 | S12 | c-use |
| M3-DU12 | owner | S13 | S14 | p-use |
| M3-DU13 | owner | S13 | S15 | c-use |
| M3-DU14 | jockey | S17 | S18 | p-use |
| M3-DU15 | jockey | S17 | S19 | c-use |
| M3-DU16 | dto | S7 | S20 | c-use |

### M4 – checkAllHorsesHealth (9 DU pairs)
| DU ID | Variable | Definition | Use | Type |
|---|---|---|---|---|
| M4-DU1 | raceId | S0 | S1 | c-use |
| M4-DU2 | raceId | S0 | S3 | c-use |
| M4-DU3 | raceId | S0 | S7 | c-use |
| M4-DU4 | healthChecks | S1 | S2 | p-use |
| M4-DU5 | healthChecks | S1 | S12 | c-use |
| M4-DU6 | participants | S3 | S4 | c-use |
| M4-DU7 | participant | S4 | S6 | c-use |
| M4-DU8 | check | S5 | S10 | c-use |
| M4-DU9 | saved | S10 | S11 | c-use |

### M5 – registerHorseToTournament (8 DU pairs + 1 anomaly)
| DU ID | Variable | Definition | Use | Type |
|---|---|---|---|---|
| M5-DU1 | dto | S0 | S1 | c-use |
| M5-DU2 | dto | S0 | S4 | c-use |
| M5-DU3 | dto | S0 | S5 | c-use |
| M5-DU4 | dto | S0 | S8 | c-use |
| M5-DU5 | dto | S0 | S9 | c-use |
| M5-DU6 | dto | S0 | S10 | c-use |
| M5-DU7 | tournament | S1 | S2 | p-use |
| M5-DU8 | registration | S7 | S13 | c-use |
| M5-ANOMALY | horse | S4 | *(không có)* | — |

### M6 – advanceTournament (20 DU pairs)
| DU ID | Variable | Definition | Use | Type |
|---|---|---|---|---|
| M6-DU1 | raceId | S0 | S1 | c-use |
| M6-DU2 | raceId | S0 | S8 | c-use |
| M6-DU3 | race | S1 | S2 | c-use |
| M6-DU4 | race | S1 | S3 | c-use |
| M6-DU5 | race | S1 | S14 | c-use |
| M6-DU6 | advancingCount | S2 | S4 | p-use |
| M6-DU7 | advancingCount | S2 | S11 | c-use |
| M6-DU8 | advancingCount | S2 | S26 | c-use |
| M6-DU9 | tournament | S3 | S5 | c-use |
| M6-DU10 | tournament | S3 | S6 | c-use |
| M6-DU11 | tournament | S3 | S13 | c-use |
| M6-DU12 | results | S8 | S9 | p-use |
| M6-DU13 | results | S8 | S11 | c-use |
| M6-DU14 | topHorses | S11 | S18 | c-use |
| M6-DU15 | nextRace | S12 | S17 | c-use |
| M6-DU16 | savedNextRace | S17 | S20 | c-use |
| M6-DU17 | result (loop) | S18 | S21 | c-use |
| M6-DU18 | result (loop) | S18 | S22 | c-use |
| M6-DU19 | registration (loop) | S19 | S25 | c-use |
| M6-DU20 | registration (loop, redefine mỗi vòng) | S19 (lần lặp k) | S25 (lần lặp k) | c-use |

### M7 – updateRace/validateRace (16 DU pairs)
| DU ID | Variable | Definition | Use | Type |
|---|---|---|---|---|
| M7-DU1 | raceDTO.tournamentId | S0 | V1 | p-use |
| M7-DU2 | raceDTO.tournamentId | S0 | V2 | p-use |
| M7-DU3 | raceDTO.tournamentId | S0 | U2 | c-use |
| M7-DU4 | raceDTO.name | S0 | V3 | p-use |
| M7-DU5 | raceDTO.name | S0 | U3 | c-use |
| M7-DU6 | raceDTO.startTime | S0 | V4 | p-use |
| M7-DU7 | raceDTO.startTime | S0 | U4 | c-use |
| M7-DU8 | raceDTO.distance | S0 | V5 | p-use |
| M7-DU9 | raceDTO.distance | S0 | U5 | c-use |
| M7-DU10 | raceDTO.status | S0 | V6 | p-use |
| M7-DU11 | raceDTO.status | S0 | U6 | c-use |
| M7-DU12 | raceDTO.refereeId | S0 | U7 | c-use |
| M7-DU13 | id | S0 | U1 | c-use |
| M7-DU14 | existingRace | U1 | U2–U7 (nhóm) | c-use |
| M7-DU15 | existingRace | U1 | U8 | c-use |
| M7-DU16 | updatedRace | U8 | U9 | c-use |

### M8 – assignJockeyToRegistration (6 DU pairs)
| DU ID | Variable | Definition | Use | Type |
|---|---|---|---|---|
| M8-DU1 | registrationId | S0 | S1 | c-use |
| M8-DU2 | jockeyId | S0 | S4 | c-use |
| M8-DU3 | jockeyId | S0 | S5 | c-use |
| M8-DU4 | registration | S1 | S2 | p-use |
| M8-DU5 | registration | S1 | S5 | c-use |
| M8-DU6 | registration | S1 | S6 | c-use |

### M9 – buildScheduleDTO (18 DU pairs)
| DU ID | Variable | Definition | Use | Type |
|---|---|---|---|---|
| M9-DU1 | reg | S0 | S2 | c-use |
| M9-DU2 | reg | S0 | S3 | p-use |
| M9-DU3 | reg | S0 | S4 | p-use |
| M9-DU4 | reg | S0 | S5 | c-use |
| M9-DU5 | reg | S0 | S25 | c-use |
| M9-DU6 | reg | S0 | S28 | p-use |
| M9-DU7 | reg | S0 | S29 | c-use |
| M9-DU8 | registrationRef | S5 | S6 | c-use |
| M9-DU9 | registrationRef | S5 | S18 | c-use |
| M9-DU10 | race | S6 | S7 | p-use |
| M9-DU11 | race | S6 | S8/S9 | c-use |
| M9-DU12 | race | S6 | S10 | p-use |
| M9-DU13 | race | S6 | S17 | c-use |
| M9-DU14 | tournament(S11) | S11 | S12–S16 | c-use |
| M9-DU15 | tournament2(S18) | S18 | S19–S24 | c-use |
| M9-DU16 | horse | S25 | S26/S27 | c-use |
| M9-DU17 | dto | S1 | S31 | c-use |
| M9-DU18 | dto | S1 | (chuỗi setter S2..S30) | c-use |

**Tổng DU pairs xác định được: 11+16+16+9+8+20+16+6+18 = 120 DU pairs** (+ 1 anomaly define-without-use ở M5).

---

## PHẦN 6 – XÁC ĐỊNH TIÊU CHÍ DATA FLOW TESTING

- **All-Defs:** với mỗi Definition, chọn ít nhất 1 Use bất kỳ để test. Áp dụng được cho toàn bộ 9 method — chi phí thấp nhất nhưng độ mạnh phát hiện lỗi thấp nhất.
- **All-Uses:** với mỗi Definition, phải test **tất cả** các Use tương ứng (cả c-use và p-use). Đây là tiêu chí có tính khả thi tốt nhất cho project này vì:
    - Số lượng DU pairs (120) không quá lớn để thực thi thủ công/bán tự động qua Postman/JUnit trong phạm vi 1 đồ án.
    - Đảm bảo bao phủ cả 2 loại use (tính toán và điều kiện) — quan trọng vì phần lớn bug tiềm ẩn nằm ở p-use (ví dụ M2-DU8: `position` dùng trong điều kiện `position == 99`).
- **All DU-Paths:** yêu cầu test **mọi đường đi** từ Definition đến Use mà không bị kill giữa chừng. Với các method có nhánh lồng nhau (M3: 7 điểm rẽ nhánh, M9: 6 đường đi khả thi), số lượng path tăng nhanh (M3 có thể lên tới hàng chục tổ hợp True/False độc lập). → **Không khả thi áp dụng toàn bộ All DU-Paths cho M3, M6 trong phạm vi đồ án** vì số path quá lớn so với thời gian thực thi thủ công.

**Quyết định:** chọn **All-Uses làm tiêu chí chính** cho toàn bộ 9 method. Riêng M9 (chỉ 6 đường đi khả thi, đã liệt kê ở Phần 4) áp dụng thêm **All DU-Paths** vì khả thi và có giá trị minh họa cho báo cáo/thuyết trình.

---

## PHẦN 7 – THIẾT KẾ TEST CASE

*Test case được thiết kế bám trực tiếp theo các DU pairs ở Phần 5, không tạo ngẫu nhiên. Mỗi TC ghi rõ DU covered theo mã đã định nghĩa.*

| TC ID | Method | Input | Precondition | Expected Result | DU covered |
|---|---|---|---|---|---|
| TC01 | createRaceResult | raceId, horseId hợp lệ, chưa có kết quả | Race & Horse tồn tại, chưa có RaceResult cho cặp (raceId,horseId) | Tạo thành công, trả về RaceResultDTO với prizeMoney tính theo position | M1-DU1..DU8, DU10, DU11 |
| TC02 | createRaceResult | raceId+horseId đã có RaceResult | Đã tồn tại RaceResult trùng | Ném RuntimeException "Race result already exists..." | M1-DU9 |
| TC03 | createRaceResult | position = 1 | — | prizeMoney = 1000.0 (nhánh `case 1` của `calculatePrizeMoney`) | M1-DU7 |
| TC04 | createRaceResult | position = 99 (bị loại) | — | prizeMoney = 100.0 (nhánh `default`) | M1-DU7 |
| TC05 | updateRaceResult | version đúng, position khác 99 | RaceResult tồn tại | Cập nhật thành công, KHÔNG chạy vòng lặp shift | M2-DU1..DU10, DU12 |
| TC06 | updateRaceResult | version sai (khác version hiện tại) | RaceResult tồn tại | Ném `OptimisticLockingFailureException` | M2-DU2, DU3 |
| TC07 | updateRaceResult | resultId không tồn tại | — | Ném RuntimeException "Race result not found" | M2-DU1 (D không tới U vì thoát sớm — negative case) |
| TC08 | updateRaceResult | oldPosition=2, position mới=99, có 3 kết quả khác vị trí 3,4,5 | RaceResult tồn tại, version đúng | Các kết quả vị trí 3,4,5 bị trừ 1 (→2,3,4); kết quả bị đổi thành 99 | M2-DU11, DU13, DU14, DU15, DU16 |
| TC09 | updateRaceResult | oldPosition=99, position mới=99 | — | KHÔNG chạy vòng lặp shift (do `oldPosition != 99` = false) | M2-DU11 (nhánh False) |
| TC10 | getHorsesByRace | raceId có registration trực tiếp | Registration.findByRaceId trả về danh sách khác rỗng | Không gọi fallback tournamentId | M3-DU3 (false), DU4 |
| TC11 | getHorsesByRace | raceId không có registration trực tiếp nhưng Race có tournamentId hợp lệ | registrations rỗng ban đầu | Fallback tìm theo tournamentId, trả kết quả bổ sung | M3-DU3(true), DU5, DU6 |
| TC12 | getHorsesByRace | registration có horseId hợp lệ & horse có ownerId hợp lệ | Horse, Owner tồn tại | dto có đầy đủ horseName, ownerName | M3-DU7..DU13 |
| TC13 | getHorsesByRace | registration có horseId nhưng horse không tồn tại trong DB | horseId "orphan" | dto không set thông tin horse (horse=null) | M3-DU10 (nhánh false) |
| TC14 | getHorsesByRace | registration.jockeyId = null | — | dto không set jockeyName | M3-DU9 (nhánh false) |
| TC15 | checkAllHorsesHealth | raceId chưa có health check nào | healthChecks rỗng | Tạo mới health check "HEALTHY" cho từng participant và lưu DB | M4-DU4(true), DU6..DU9 |
| TC16 | checkAllHorsesHealth | raceId đã có health check | healthChecks khác rỗng | Trả về danh sách có sẵn, KHÔNG tạo mới | M4-DU4(false), DU5 |
| TC17 | registerHorseToTournament | tournamentId, horseId hợp lệ, giải đấu UPCOMING, chưa đăng ký | — | Đăng ký thành công, status=PENDING | M5-DU1..DU8 |
| TC18 | registerHorseToTournament | giải đấu status=COMPLETED | — | Ném lỗi "Giải đấu này đã đóng..." | M5-DU7 (true) |
| TC19 | registerHorseToTournament | horseId đã đăng ký giải đấu này rồi | registrationRepository.existsByRaceIdAndHorseId = true | Ném lỗi "Con ngựa này đã được đăng ký..." | M5-DU3 (nhánh true tại S5) |
| TC20 | advanceTournament | race.advancingCount = 3 | — | Tournament chuyển COMPLETED, trả về thông báo vòng chung kết | M6-DU6(true), DU9, DU10 |
| TC21 | advanceTournament | race.advancingCount = 8, có đủ RaceResult | — | Tạo Race mới + đăng ký Top 8 vào vòng mới (status APPROVED) | M6-DU6(false), DU12..DU20 |
| TC22 | advanceTournament | race.advancingCount = 8 nhưng chưa có RaceResult nào | results rỗng | Ném lỗi "Vòng đua này chưa có kết quả để xét loại!" | M6-DU12 (true) |
| TC23 | advanceTournament | race.advancingCount = null | — | Dùng mặc định = 3 → coi như vòng chung kết | M6-DU6 (giá trị mặc định qua c-use ban đầu) |
| TC24 | updateRace | raceDTO thiếu tournamentId | — | Ném lỗi "Vui lòng chọn giải đấu..." | M7-DU1 (true) |
| TC25 | updateRace | tournamentId không tồn tại trong DB | — | Ném lỗi "Không tìm thấy giải đấu..." | M7-DU2 (true) |
| TC26 | updateRace | distance = 0 | Qua được V1–V4 | Ném lỗi "Quãng đường cuộc đua phải lớn hơn 0." | M7-DU8 (true) |
| TC27 | updateRace | raceDTO hợp lệ đầy đủ | id tồn tại | Cập nhật thành công, trả RaceDTO mới | M7-DU1..DU16 |
| TC28 | assignJockeyToRegistration | registration chưa có jockey | jockeyId tồn tại | Gán thành công | M8-DU1..DU6 |
| TC29 | assignJockeyToRegistration | registration đã có jockey | — | Ném lỗi "Registration đã có Jockey, không thể gán lại." | M8-DU4 (true) |
| TC30 | buildScheduleDTO (qua getScheduleByJockeyId) | reg.raceId trỏ tới 1 Race có tournamentId | Race tồn tại | dto có đủ raceId + thông tin tournament lồng trong race | M9-DU10(true), DU12(true), DU14 |
| TC31 | buildScheduleDTO (qua getOwnerRegistrations) | reg.raceId thực chất trỏ tới Tournament (không phải Race) | raceRepository.findById trả về Optional.empty | Nhánh else: lấy thông tin trực tiếp từ Tournament | M9-DU10(false), DU15 |
| TC32 | buildScheduleDTO | reg.jockeyId = null | — | dto không set jockeyName | M9-DU6(false) |
| TC33 | buildScheduleDTO | reg.jockeyId hợp lệ | Jockey tồn tại | dto.jockeyName được set | M9-DU6(true), DU7 |

---

## PHẦN 8 – POSTMAN / JUNIT

Endpoint được lấy **trực tiếp từ Controller** (không đoán). Bảng ánh xạ TC → công cụ thực thi:

| Method (service) | Controller & Endpoint thật | Công cụ đề xuất |
|---|---|---|
| createRaceResult | `POST /api/referee/race/{raceId}/result` (RefereeController) | Postman |
| updateRaceResult | `PUT /api/referee/result/{resultId}` (RefereeController) | Postman |
| getHorsesByRace | `GET /api/referee/race/{raceId}/horses` (RefereeController, public GET, không cần token) | Postman |
| checkAllHorsesHealth | `GET /api/referee/race/{raceId}/health-check/all` (RefereeController, public GET) | Postman |
| registerHorseToTournament | `POST /api/admin/tournaments/{id}/register` (TournamentController) **hoặc** `POST /api/v1/registrations/register` (TournamentRegistrationController) — **2 controller khác nhau cùng gọi 1 service method** | Postman (test cả 2 endpoint) |
| advanceTournament | `POST /api/admin/tournaments/advance/{raceId}` (TournamentController) | Postman |
| updateRace | `PUT /api/admin/races/{id}` (RaceController) | Postman |
| assignJockeyToRegistration | `PUT /api/v1/registrations/{registrationId}/assign-jockey/{jockeyId}` (RegistrationController) | Postman |
| buildScheduleDTO (private) | Gián tiếp qua `GET /api/v1/registrations/jockey/{jockeyId}/schedule` hoặc `GET /api/v1/registrations/owner/{ownerId}/requests` (RegistrationController) | **JUnit/SpringBootTest** (test qua wrapper method public vì `buildScheduleDTO` là private, không có endpoint riêng — không được suy đoán endpoint không tồn tại) |

### Chi tiết mẫu Postman cho TC01 (createRaceResult)
- **HTTP Method:** POST
- **Endpoint:** `{{baseUrl}}/api/referee/race/{{raceId}}/result`
- **Headers:** `Content-Type: application/json`
- **Authorization:** endpoint này KHÔNG nằm trong danh sách `permitAll()` của `SecurityConfig` (chỉ `GET /api/referee/race/**` được permitAll) → cần Bearer Token hợp lệ (`Authorization: Bearer <token>`)
- **Request Body:**
```json
{
  "horseId": "horse_001",
  "jockeyId": "jockey_001",
  "position": 1,
  "finishTime": 65.4
}
```
- **Expected Status Code:** 200 (theo cách `RefereeController` bọc mọi kết quả thành công trong `ResponseEntity.ok(...)`, kể cả logic bên trong ném exception thì trả 400)
- **Expected Response:** JSON `RaceResultDTO` có `prizeMoney = 1000.0` (vì position=1)
- **Postman test script:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});
pm.test("Prize money is correct for position 1", function () {
    const body = pm.response.json();
    pm.expect(body.prizeMoney).to.eql(1000.0);
});
```

### Chi tiết mẫu Postman cho TC02 (trùng kết quả)
- **HTTP Method / Endpoint:** như TC01
- **Precondition:** đã gọi TC01 trước đó với cùng raceId/horseId
- **Expected Status Code:** 400 (do controller bắt exception và trả `badRequest()`)
- **Expected Response:** `{"error": "Race result already exists for this horse in this race"}`
- **Postman test script:**
```javascript
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});
pm.test("Error message matches", function () {
    pm.expect(pm.response.json().error).to.include("already exists");
});
```

### Chi tiết mẫu Postman cho TC08 (shift vị trí khi loại ngựa)
- **HTTP Method:** PUT
- **Endpoint:** `{{baseUrl}}/api/referee/result/{{resultId}}`
- **Authorization:** Bearer Token (không nằm trong permitAll)
- **Request Body:**
```json
{
  "position": 99,
  "finishTime": 0,
  "prizeMoney": 0,
  "version": 0
}
```
- **Expected Status Code:** 200
- **Postman test script:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});
// Sau đó cần gọi thêm GET /api/referee/race/{raceId}/results để verify các vị trí khác đã được shift -1
```

### JUnit mẫu cho M9 (buildScheduleDTO) — vì là private method, test qua public wrapper
```java
@SpringBootTest
class RegistrationServiceImplTest {

    @Autowired
    private RegistrationService registrationService;

    @Test
    void getScheduleByJockeyId_shouldPopulateRaceInfo_whenRaceIdIsActualRace() {
        // Arrange: reg.raceId trỏ tới 1 Race thật (nhánh S7 = true)
        List<JockeyScheduleDTO> result = registrationService.getScheduleByJockeyId("jockey_001");
        // Assert
        assertFalse(result.isEmpty());
        assertNotNull(result.get(0).getRaceId());
    }

    @Test
    void getOwnerRegistrations_shouldFallbackToTournament_whenRaceIdIsTournament() {
        // Arrange: reg.raceId thực chất là tournamentId (nhánh S7 = false)
        List<JockeyScheduleDTO> result = registrationService.getOwnerRegistrations("owner_001");
        assertFalse(result.isEmpty());
        assertNotNull(result.get(0).getTournamentId());
    }
}
```

*Lưu ý: các TC còn lại (TC03, TC04, TC09, TC13, TC14, TC16, TC23, TC26, TC32) mang tính kiểm tra nhánh nội bộ ở mức đơn vị — nên ưu tiên JUnit (mock Repository) thay vì Postman để cô lập được từng nhánh dễ dàng, vì qua Postman phải chuẩn bị dữ liệu MongoDB thực tế khá tốn công.*

---

## PHẦN 9 – DATA FLOW COVERAGE

⚠️ **Đây là Coverage lý thuyết (Theoretical/Expected Coverage)** — báo cáo này chưa thực thi test thật trên môi trường (chưa chạy Postman/JUnit thực tế), do đó số liệu dưới đây phản ánh **DU pairs mà bộ 33 test case ở Phần 7 được THIẾT KẾ để bao phủ**, KHÔNG phải Actual Coverage đo được từ công cụ coverage.

| Method | Total DU pairs | DU covered theo thiết kế TC | Coverage lý thuyết |
|---|---|---|---|
| M1 createRaceResult | 11 | 11 | 100% |
| M2 updateRaceResult | 16 | 14 (thiếu DU4/DU7 ở nhánh không đổi position≠hiện tại kèm finishTime cụ thể — đã cover gián tiếp qua TC05 nhưng chưa tách riêng biên) | ~87.5% |
| M3 getHorsesByRace | 16 | 13 (chưa thiết kế TC riêng cho DU11 [horse≠null nhưng thiếu ownerId] và DU13/DU16 tổ hợp hiếm) | ~81% |
| M4 checkAllHorsesHealth | 9 | 9 | 100% |
| M5 registerHorseToTournament | 8 (+1 anomaly) | 8 | 100% |
| M6 advanceTournament | 20 | 16 (TC20-23 chưa cover riêng M6-DU17/18/19/20 khi topHorses > 1 phần tử với dữ liệu jockeyId null) | 80% |
| M7 updateRace | 16 | 16 | 100% |
| M8 assignJockeyToRegistration | 6 | 6 | 100% |
| M9 buildScheduleDTO | 18 | 14 (chưa có TC cho tổ hợp race≠null nhưng tournamentId rỗng — DU12 nhánh false) | ~78% |
| **TỔNG** | **120** | **107** | **≈ 89.2%** |

**Bảng minh họa (rút gọn) DU → TC:**

| DU ID | Covered by TC | Covered? |
|---|---|---|
| M1-DU9 | TC02 | Yes |
| M2-DU8 | TC08, TC09 | Yes |
| M3-DU10 | TC13 | Yes (nhánh false); nhánh true qua TC12 |
| M5-ANOMALY | — | No (không thể "cover" một anomaly, chỉ ghi nhận bug) |
| M6-DU12 | TC22 | Yes |
| M9-DU12 (nhánh false) | — | No |

**Tổng kết:**
- Total DU pairs: **120**
- Covered (theo thiết kế 33 TC): **107**
- Uncovered: **13** (chủ yếu là các tổ hợp nhánh hiếm gặp trong M3, M6, M9 — liệt kê chi tiết ở Phần 11)
- Data Flow Coverage (Expected) = 107/120 × 100% ≈ **89.2%**

---

## PHẦN 10 – PHÂN BIỆT DATA FLOW COVERAGE VÀ CODE COVERAGE

| Loại Coverage | Đo cái gì |
|---|---|
| **Statement Coverage** | % dòng lệnh (statement) được thực thi ít nhất 1 lần |
| **Branch Coverage** | % nhánh rẽ (true/false của từng if/else) được thực thi |
| **Path Coverage** | % đường đi hoàn chỉnh (tổ hợp nhiều nhánh liên tiếp) được thực thi |
| **Data Flow Coverage** | % cặp Definition→Use của **từng biến cụ thể** được thực thi, đảm bảo giá trị được gán ra đúng có thực sự "đi tới" nơi sử dụng nó mà không bị ghi đè giữa chừng |

**Vì sao "100% code coverage không đồng nghĩa 100% Data Flow Coverage":**
Statement/Branch Coverage chỉ quan tâm dòng lệnh/nhánh có được **chạy qua** hay không, không quan tâm **giá trị dữ liệu cụ thể** đi từ đâu đến đâu. Ví dụ điển hình ngay trong `updateRaceResult` (M2): một test case có thể chạy qua đủ 100% statement và 100% branch của khối `if (oldPosition != null && oldPosition != 99 && position == 99)` (chỉ cần 1 TC true + 1 TC false), nhưng **không hề đảm bảo** rằng biến `other.getPosition()` (được Definition tại dòng lặp, redefine tại S13) đã thực sự được kiểm tra ở mọi tổ hợp giá trị `oldPosition` khác nhau (ví dụ oldPosition=1 vs oldPosition=5 cho ra tập `other` bị shift khác nhau) — đây chính là phần mà Data Flow Coverage bổ sung mà Statement/Branch Coverage bỏ sót. Tương tự, tại `registerHorseToTournament` (M5), biến `horse` có thể đạt 100% statement coverage (dòng `Horse horse = horseRepository.findById(...)` luôn được chạy) nhưng Data Flow Testing lại phát hiện ra `horse` **không hề có Use nào** — một loại lỗi mà code coverage truyền thống không bao giờ chỉ ra được.

---

## PHẦN 11 – KẾT QUẢ CUỐI CÙNG

| Class | Method | # Variables | # Definitions | # Uses | # DU pairs | # Test Cases | Coverage (lý thuyết) |
|---|---|---|---|---|---|---|---|
| RefereeServiceImpl | createRaceResult | 8 | 8 | 12 | 11 | 4 (TC01-04) | 100% |
| RefereeServiceImpl | updateRaceResult | 11 | 11 | 17 | 16 | 5 (TC05-09) | ~87.5% |
| RefereeServiceImpl | getHorsesByRace | 9 | 9 | 15 | 16 | 5 (TC10-14) | ~81% |
| RefereeServiceImpl | checkAllHorsesHealth | 6 | 6 | 8 | 9 | 2 (TC15-16) | 100% |
| TournamentServiceImpl | registerHorseToTournament | 4 | 4 | 8 | 8 (+1 anomaly) | 3 (TC17-19) | 100% |
| TournamentServiceImpl | advanceTournament | 10 | 10 | 17 | 20 | 4 (TC20-23) | 80% |
| RaceServiceImpl | updateRace (+validateRace) | 9 | 9 | 16 | 16 | 4 (TC24-27) | 100% |
| RegistrationServiceImpl | assignJockeyToRegistration | 3 | 3 | 6 | 6 | 2 (TC28-29) | 100% |
| RegistrationServiceImpl | buildScheduleDTO | 7 | 7 | 20 | 18 | 4 (TC30-33) | ~78% |
| **TỔNG** | **9 method** | **67** | **67** | **119** | **120** | **33** | **≈ 89.2%** |

**Tổng hợp số liệu:**
- Tổng số method được phân tích: **9**
- Tổng số biến (đếm theo từng method, có biến trùng tên khác scope ở method khác nhau): **67**
- Tổng số Definition: **67**
- Tổng số Use (c-use + p-use): **119**
- Tổng số DU pairs: **120** (+ 1 anomaly define-without-use)
- Tổng số Test Case: **33**
- Coverage đạt được (lý thuyết, theo thiết kế TC): **≈ 89.2%**
- DU chưa cover (13 DU): tập trung ở M2 (biên finishTime cụ thể), M3 (owner thiếu ownerId, tổ hợp hiếm), M6 (jockeyId null trong topHorses khi advance), M9 (race tồn tại nhưng tournamentId rỗng) — cần bổ sung TC nếu muốn đạt gần 100%.
- Vấn đề phát hiện được: xem Phần 12.

---

## PHẦN 12 – PHÁT HIỆN BUG

*Tất cả các mục dưới đây được phát hiện qua phân tích Definition/Use thực tế trên source code, không bịa.*

| Bug ID | File | Method | Mô tả | Steps to reproduce | Expected | Actual | Severity |
|---|---|---|---|---|---|---|---|
| BUG-01 | `TournamentServiceImpl.java` | `registerHorseToTournament` | Biến `Horse horse` (dòng `horseRepository.findById(dto.getHorseId())...`) được Definition nhưng **không có Use** nào phía sau — object lấy về chỉ dùng để kiểm tra tồn tại rồi bị bỏ, toàn bộ field khác của `Registration` (raceId, horseId,...) được lấy lại từ `dto` chứ không từ `horse`. Đây là DU-anomaly loại "define without use", cho thấy code có khả năng dư thừa hoặc thiếu logic (ví dụ có thể dự định set thêm thông tin từ `horse` nhưng bị bỏ sót). | 1. Gọi `POST /api/admin/tournaments/{id}/register` với horseId hợp lệ | Biến horse nên được sử dụng có mục đích, hoặc loại bỏ nếu chỉ cần kiểm tra tồn tại | `horse` bị bỏ không dùng sau khi Definition | Low (code smell, không gây lỗi runtime) |
| BUG-02 | `RefereeServiceImpl.java` | `updateRaceResult` | Có khả năng **NullPointerException**: điều kiện `position == 99` tại dòng `if (oldPosition != null && oldPosition != 99 && position == 99)` so sánh trực tiếp biến `Integer position` (tham số đầu vào, có thể null vì không có validate ở đầu method) — nếu client gửi `position = null`, Java sẽ tự động unbox `Integer` → `int`, gây NPE ngay tại điều kiện này. | 1. Gọi `PUT /api/referee/result/{resultId}` với body thiếu field `position` (hoặc null) | Nên trả lỗi 400 nghiệp vụ rõ ràng ("position không được để trống") | Ném `NullPointerException` không kiểm soát (bị bắt bởi `try/catch(Exception e)` ở Controller nên trả 400 nhưng thông báo lỗi không rõ nghĩa, khó debug) | Medium |
| BUG-03 | `RefereeServiceImpl.java` | `updateRaceResult` | Tương tự BUG-02, `other.getPosition() != 99` và `other.getPosition() > oldPosition` trong vòng lặp cũng unbox `Integer` trực tiếp — nếu tồn tại `RaceResult` khác trong cùng raceId có `position = null` (dữ liệu cũ/import lỗi), vòng lặp sẽ NPE và toàn bộ transaction thất bại dù mục đích ban đầu chỉ update 1 kết quả. | 1. Tạo trước 1 RaceResult có `position = null` cho cùng raceId. 2. Gọi update 1 RaceResult khác sang position=99 | Bỏ qua bản ghi có position null một cách an toàn, hoặc log cảnh báo | NPE giữa vòng lặp, dữ liệu có thể bị cập nhật dở dang (đã save 1 vài bản ghi trước khi lỗi) | High (rủi ro toàn vẹn dữ liệu — cập nhật dở dang) |
| BUG-04 | `RefereeServiceImpl.java` | `checkHorseHealth` vs `checkAllHorsesHealth` | Không nhất quán: `checkHorseHealth(horseId, raceId)` khi không tìm thấy health check thì tạo `HorseHealthCheck` mới bằng `orElseGet(...)` nhưng **không gọi `healthCheckRepository.save(...)`** trước khi convertToDTO — dữ liệu mặc định "HEALTHY" chỉ tồn tại trong response, KHÔNG được lưu DB. Trong khi đó `checkAllHorsesHealth` (M4) lại có `save(check)` đầy đủ. Gọi lại `checkHorseHealth` nhiều lần với cùng input sẽ luôn tạo "bản ghi ảo" mới mà không bao giờ persist. | 1. Gọi `GET /api/referee/horse/{horseId}/health/race/{raceId}` khi chưa có health check | Health check mặc định nên được lưu DB giống hành vi của `checkAllHorsesHealth` | Dữ liệu không được lưu, dẫn đến 2 API cho cùng nghiệp vụ có hành vi khác nhau | Medium |
| BUG-05 | `TournamentServiceImpl.java` | `advanceTournament` | `Race nextRace` được tạo và lưu trực tiếp qua `raceRepository.save(nextRace)` mà **không đi qua `RaceServiceImpl.validateRace`** (không set `startTime`, không set `distance`, không set `refereeId`). Điều này phá vỡ tính bất biến (invariant) mà chính hệ thống áp đặt ở nơi khác (M7: `validateRace` bắt buộc `startTime` và `distance > 0` khi tạo/sửa Race qua `RaceController`). Race mới sinh ra sẽ có `startTime = null`, `distance = null` cho tới khi admin sửa tay. | 1. Gọi `POST /api/admin/tournaments/advance/{raceId}` với advancingCount > 3 | Race mới nên có dữ liệu hợp lệ tối thiểu hoặc được đánh dấu rõ "cần admin hoàn thiện" | Race được persist ở trạng thái dữ liệu không đầy đủ, vi phạm invariant nghiệp vụ định nghĩa ở RaceServiceImpl | Medium |
| BUG-06 | `RegistrationServiceImpl.java` | `assignJockeyToRegistration` | Method không kiểm tra `registration.getStatus()` trước khi gán jockey — có thể gán jockey cho 1 `Registration` đã ở trạng thái `REJECTED`, dẫn tới dữ liệu nghiệp vụ vô lý (đăng ký bị từ chối nhưng vẫn có jockey được gán). | 1. Tạo registration, gọi `rejectRegistrationByJockey` để set REJECTED. 2. Gọi `assignJockeyToRegistration` cho registration đó | Nên chặn gán jockey cho registration đã REJECTED | Gán thành công bình thường (không có check) | Low–Medium |
| BUG-07 | `RegistrationServiceImpl.java` | `buildScheduleDTO` | `tournament.getStartDate().toString()` / `.getEndDate().toString()` gọi trực tiếp không kiểm tra null — mặc dù `validateTournament` (RaceServiceImpl không liên quan, đây là TournamentServiceImpl) bắt buộc các field này khi tạo mới, nhưng nếu dữ liệu được tạo trực tiếp trong DB (seed/migration) hoặc qua code path khác không đi qua `validateTournament`, sẽ gây NPE khi hiển thị lịch trình. | 1. Tạo Tournament trực tiếp trong MongoDB thiếu `startDate`. 2. Gọi `GET /api/v1/registrations/jockey/{jockeyId}/schedule` | Nên kiểm tra null trước khi `.toString()` | NPE nếu startDate/endDate null | Low |

---

