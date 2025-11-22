package B2A4.demoday.domain.medication.service;

import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.medication.dto.request.MedicationRequest;
import B2A4.demoday.domain.medication.dto.request.ScheduleRequest;
import B2A4.demoday.domain.medication.dto.response.MedicationDailyResponse;
import B2A4.demoday.domain.medication.dto.response.MedicationMonthlyResponse;
import B2A4.demoday.domain.medication.dto.response.MedicationResponse;
import B2A4.demoday.domain.medication.entity.MedicationHistory;
import B2A4.demoday.domain.medication.entity.MedicationRecord;
import B2A4.demoday.domain.medication.entity.MedicationSchedule;
import B2A4.demoday.domain.medication.repository.MedicationHistoryRepository;
import B2A4.demoday.domain.medication.repository.MedicationRecordRepository;
import B2A4.demoday.domain.medication.repository.MedicationScheduleRepository;
import B2A4.demoday.domain.patient.entity.Patient;
import B2A4.demoday.domain.patient.repository.PatientRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final PatientRepository patientRepository;
    private final MedicationRecordRepository medicationRecordRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationHistoryRepository historyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 복약 일정 추가
    public MedicationResponse createMedication(Long patientId, MedicationRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NoSuchElementException("환자를 찾을 수 없습니다."));

        // 요일 리스트(List<String>) → JSON 문자열로 변환 (DB 저장용)
        String daysOfWeekJson;
        try {
            daysOfWeekJson = objectMapper.writeValueAsString(request.getDaysOfWeek());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("요일 데이터를 변환할 수 없습니다.");
        }

        // 어떤 환자가 어떤 약을 언제부터 언제까지 복용하는지 저장
        MedicationRecord record = MedicationRecord.builder()
                .patient(patient)
                .name(request.getName())
                .startDate(LocalDate.parse(request.getStartDate()))
                .endDate(LocalDate.parse(request.getEndDate()))
                .alarmEnabled(request.getAlarmEnabled())
                .daysOfWeek(daysOfWeekJson)
                .build();

        medicationRecordRepository.save(record);

        List<MedicationSchedule> schedules = request.getSchedules().stream()
                .map(s -> MedicationSchedule.builder()
                        .medicationRecord(record)
                        .period(s.getPeriod())
                        .time(LocalTime.parse(s.getTime()))
                        .enabled(s.getEnabled())
                        .build())
                .collect(Collectors.toList());

        medicationScheduleRepository.saveAll(schedules);

        record.getSchedules().addAll(schedules);

        return MedicationResponse.from(record);
    }

    // 복약 일정 수정
    @Transactional
    public CommonResponse<MedicationResponse> updateMedication(Long recordId, MedicationRequest request) {

        // 기존 복약 일정 조회
        MedicationRecord record = medicationRecordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchElementException("해당 복약 일정을 찾을 수 없습니다."));

        record.getPatient();

        // 요청에 포함된 항목만 업데이트 (null 체크)
        if (request.getName() != null) {
            record.updateName(request.getName());
        }
        if (request.getStartDate() != null) {
            record.updateStartDate(LocalDate.parse(request.getStartDate()));
        }
        if (request.getEndDate() != null) {
            record.updateEndDate(LocalDate.parse(request.getEndDate()));
        }
        if (request.getAlarmEnabled() != null) {
            record.updateAlarmEnabled(request.getAlarmEnabled());
        }
        if (request.getDaysOfWeek() != null) {
            try {
                record.updateDaysOfWeek(objectMapper.writeValueAsString(request.getDaysOfWeek()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("요일 데이터를 변환할 수 없습니다.");
            }
        }

        // 기존 스케줄 삭제 후 새 스케줄로 교체 (요청에 schedules 있을 때만)
        if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
            medicationScheduleRepository.deleteAll(record.getSchedules());

            List<MedicationSchedule> newSchedules = request.getSchedules().stream()
                    .map(s -> MedicationSchedule.builder()
                            .medicationRecord(record)
                            .period(s.getPeriod())
                            .time(LocalTime.parse(s.getTime()))
                            .enabled(s.getEnabled())
                            .build())
                    .collect(Collectors.toList());

            medicationScheduleRepository.saveAll(newSchedules);
            record.getSchedules().clear();
            record.getSchedules().addAll(newSchedules);
        }

        medicationRecordRepository.save(record);

        MedicationResponse response = MedicationResponse.from(record);
        return CommonResponse.success(response, "복약 일정이 수정되었습니다.");
    }

    // 복약 일정 조회
    @Transactional(readOnly = true)
    public List<MedicationResponse> getMedications(Long patientId, String dateStr) {

        LocalDate targetDate = (dateStr != null && !dateStr.isBlank())
                ? LocalDate.parse(dateStr)
                : null;

        List<MedicationRecord> allRecords = medicationRecordRepository.findByPatient_Id(patientId);

        List<MedicationRecord> filteredRecords = (targetDate == null)
                ? allRecords
                : allRecords.stream()
                .filter(r ->
                        (r.getStartDate() == null || !r.getStartDate().isAfter(targetDate)) &&
                                (r.getEndDate() == null || !r.getEndDate().isBefore(targetDate)))
                .collect(Collectors.toList());

        return filteredRecords.stream()
                .map(MedicationResponse::from)
                .collect(Collectors.toList());
    }

    // 특정 달 복약 일정 조회
    @Transactional(readOnly = true)
    public List<MedicationMonthlyResponse> getMonthlyMedications(Long patientId, int year, int month) {

        List<MedicationRecord> records = medicationRecordRepository.findByPatient_Id(patientId);

        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        return records.stream()
                .map(record -> {

                    // 기간 안 겹치면 제외
                    if (record.getEndDate().isBefore(firstDay) || record.getStartDate().isAfter(lastDay)) {
                        return null;
                    }

                    // 요일 JSON 파싱
                    List<String> daysOfWeek = parseDaysOfWeek(record.getDaysOfWeek());

                    // 실제 해당 달 날짜 계산
                    List<String> actualDays = calculateMedicationDays(
                            record.getStartDate(),
                            record.getEndDate(),
                            daysOfWeek,
                            year, month
                    );

                    if (actualDays.isEmpty()) return null;

                    return MedicationMonthlyResponse.builder()
                            .recordId(record.getId())
                            .name(record.getName())
                            .days(actualDays)
                            .build();
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    // JSON → 요일 리스트 파싱 함수
    private List<String> parseDaysOfWeek(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    // 실제 해당 달 날짜 계산 함수
    private List<String> calculateMedicationDays(
            LocalDate startDate,
            LocalDate endDate,
            List<String> daysOfWeek,
            int year,
            int month
    ) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        List<String> result = new ArrayList<>();
        LocalDate curr = firstDay;

        while (!curr.isAfter(lastDay)) {

            // 기록 범위 안의 날짜만
            if (!curr.isBefore(startDate) && !curr.isAfter(endDate)) {

                String dow = curr.getDayOfWeek().name().substring(0, 3); // MON/TUE...

                if (daysOfWeek.contains(dow)) {
                    result.add(curr.toString());
                }
            }

            curr = curr.plusDays(1);
        }

        return result;
    }

    // 복약 일정 조회(특정 날짜)
    @Transactional(readOnly = true)
    public List<MedicationDailyResponse> getDailyMedications(Long patientId, String dateStr) {

        LocalDate date = LocalDate.parse(dateStr);

        // 환자 약 전체 조회
        List<MedicationRecord> records = medicationRecordRepository.findByPatient_Id(patientId);

        // 날짜 기준 필터링
        List<MedicationRecord> filtered = records.stream()
                .filter(r ->
                        (r.getStartDate() == null || !r.getStartDate().isAfter(date)) &&
                                (r.getEndDate() == null || !r.getEndDate().isBefore(date))
                )
                .toList();

        List<MedicationDailyResponse> result = new ArrayList<>();

        for (MedicationRecord record : filtered) {

            // 해당 record의 당일 history
            List<MedicationHistory> histories =
                    historyRepository.findAllByMedicationRecord_IdAndDate(record.getId(), date);

            for (MedicationSchedule schedule : record.getSchedules()) {

                // schedule.period 에 대응하는 history 찾아보기
                MedicationHistory matched = histories.stream()
                        .filter(h -> h.getPeriod().equals(schedule.getPeriod()))
                        .findFirst()
                        .orElse(null);

                result.add(
                        MedicationDailyResponse.builder()
                                .recordId(record.getId())
                                .name(record.getName())
                                .period(schedule.getPeriod())
                                .time(schedule.getTime().toString())
                                .taken(matched != null ? matched.getTaken() : null)
                                .build()
                );
            }
        }

        return result;
    }

    // 복약 일정 삭제(record 삭제)
    @Transactional
    public void deleteMedication(Long patientId, Long recordId) {
        MedicationRecord record = medicationRecordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchElementException("해당 복약 일정을 찾을 수 없습니다."));

        if (!record.getPatient().getId().equals(patientId)) {
            throw new SecurityException("본인 복약 일정만 삭제할 수 있습니다.");
        }

        medicationRecordRepository.delete(record);
    }

    // 복약 일정 삭제(해당 날짜 OR 해당 날짜 포함 이후까지)
    @Transactional
    public void deleteMedicationByDate(Long patientId, Long recordId, String targetDateStr, String type) {

        MedicationRecord record = medicationRecordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchElementException("해당 복약 일정을 찾을 수 없습니다."));

        if (!record.getPatient().getId().equals(patientId)) {
            throw new SecurityException("본인 복약 일정만 삭제할 수 있습니다.");
        }

        LocalDate targetDate = LocalDate.parse(targetDateStr);

        LocalDate start = record.getStartDate();
        LocalDate end = record.getEndDate();

        if (targetDate.isBefore(start) || targetDate.isAfter(end)) {
            throw new IllegalArgumentException("해당 날짜는 복약 일정 범위에 포함되지 않습니다.");
        }

        // 1. 해당 날짜만 제거
        if (type.equals("single")) {

            // targetDate == startDate & endDate → 전체 삭제
            if (start.equals(end)) {
                medicationRecordRepository.delete(record);
                return;
            }

            // targetDate == startDate → startDate += 1
            if (targetDate.equals(start)) {
                record.updateStartDate(start.plusDays(1));
                return;
            }

            // targetDate == endDate → endDate -= 1
            if (targetDate.equals(end)) {
                record.updateEndDate(end.minusDays(1));
                return;
            }

            // other → 2개로 분할
            MedicationRecord newRecord = MedicationRecord.builder()
                    .patient(record.getPatient())
                    .name(record.getName())
                    .startDate(targetDate.plusDays(1))
                    .endDate(end)
                    .alarmEnabled(record.getAlarmEnabled())
                    .daysOfWeek(record.getDaysOfWeek())
                    .build();

            medicationRecordRepository.save(newRecord);

            // 기존 record는 앞부분만 남김
            record.updateEndDate(targetDate.minusDays(1));
            return;
        }

        // 2. 해당 날짜 포함 이후 삭제 (endDate = targetDate - 1)
        else if (type.equals("after")) {

            if (targetDate.equals(start)) {
                // 시작부터 모두 삭제하는 경우 → 전체 삭제
                medicationRecordRepository.delete(record);
                return;
            }

            record.updateEndDate(targetDate.minusDays(1));
            return;
        }

        throw new IllegalArgumentException("잘못된 삭제 타입입니다. type은 'single' 또는 'after' 이어야 합니다.");
    }
}