```mermaid
graph TD
    A([Bắt đầu]) --> B[Khối try: Gọi raceService.createRace]
    B --> C{"Có văng lỗi RuntimeException?"}
    
    C -->|"Có (True)"| D[Khối catch: Trả về 400 Bad Request]
    D --> F([Kết thúc])
    
    C -->|"Không (False)"| E[Thành công: Trả về 200 OK]
    E --> F

    N1([Bắt đầu]) --> N2[Khối try: Thực thi raceService.deleteRace]
    N2 --> N3{"Có văng lỗi RuntimeException?"}
    
    N3 -->|"Có (True)"| N4[Khối catch: Trả về 400 Bad Request]
    N4 --> N6([Kết thúc])
    
    N3 -->|"Không (False)"| N5[Thành công: Khởi tạo thông báo và trả về 200 OK]
    N5 --> N6