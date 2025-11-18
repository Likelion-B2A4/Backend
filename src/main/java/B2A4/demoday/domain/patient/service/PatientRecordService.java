package B2A4.demoday.domain.patient.service;

import B2A4.demoday.domain.chat.entity.ChatRoom;
import B2A4.demoday.domain.chat.repository.ChatRoomRepository;
import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.patient.dto.response.PatientRecordDatesResponse;
import B2A4.demoday.domain.patient.dto.response.PatientRecordDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientRecordService {

    private final ChatRoomRepository chatRoomRepository;

    // 특정 달 진료이력 날짜 목록 조회
    public CommonResponse<PatientRecordDatesResponse> getRecordDates(Long patientId, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);

        // finishedAt(진료 종료 시간)이 해당 월에 있는 chatRoom 조회
        List<ChatRoom> records = chatRoomRepository
                .findAllByPatientIdAndFinishedAtBetween(patientId, start.atStartOfDay(), end.atStartOfDay());

        List<String> dates = records.stream()
                .map(room -> room.getFinishedAt().toLocalDate().toString())
                .distinct()
                .sorted()
                .toList();

        return CommonResponse.success(
                new PatientRecordDatesResponse(year, month, dates),
                "진료이력 날짜 목록 조회 성공"
        );
    }

    // 특정 날짜 진료 기록 조회
    public CommonResponse<PatientRecordDetailResponse> getRecordDetail(Long patientId, String dateStr) {

        LocalDate date = LocalDate.parse(dateStr);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        // 해당 날짜 범위에 finishedAt(진료 종료 시간)이 포함된 chatRoom 조회 (1개만 존재해야 정상)
        ChatRoom record = chatRoomRepository
                .findByPatientIdAndFinishedAtBetween(patientId, start, end)
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜의 기록이 없습니다."));

        return CommonResponse.success(
                PatientRecordDetailResponse.from(record),
                "진료이력 조회 성공"
        );
    }
}