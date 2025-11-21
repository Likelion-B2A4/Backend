package B2A4.demoday.domain.medication.service;

import B2A4.demoday.domain.medication.dto.request.MedicationHistoryRequest;
import B2A4.demoday.domain.medication.dto.response.MedicationHistoryResponse;
import B2A4.demoday.domain.medication.entity.MedicationHistory;
import B2A4.demoday.domain.medication.repository.MedicationHistoryRepository;
import B2A4.demoday.domain.medication.repository.MedicationRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.medication.entity.MedicationRecord;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MedicationHistoryService {
    private final MedicationRecordRepository medicationRecordRepository;
    private final MedicationHistoryRepository medicationHistoryRepository;

    // 복용 일정 업데이트
    @Transactional
    public CommonResponse<MedicationHistoryResponse> updateHistory(
            Long patientId,
            Long recordId,
            MedicationHistoryRequest request
    ) {

        MedicationRecord record = medicationRecordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchElementException("해당 복약 일정을 찾을 수 없습니다."));

        if (!record.getPatient().getId().equals(patientId)) {
            throw new SecurityException("본인의 복약 기록만 수정할 수 있습니다.");
        }

        LocalDate date = LocalDate.parse(request.getDate());

        // 날짜 + period 기준으로 history 찾기(없으면 새로 생성)
        MedicationHistory history = medicationHistoryRepository
                .findByMedicationRecord_IdAndDateAndPeriod(
                        recordId,
                        date,
                        request.getPeriod()
                )
                .orElseGet(() ->
                        MedicationHistory.builder()
                                .medicationRecord(record)
                                .date(date)
                                .period(request.getPeriod())
                                .taken(false)
                                .build()
                );

        history.updateTaken(request.getTaken());

        medicationHistoryRepository.save(history);

        MedicationHistoryResponse response = MedicationHistoryResponse.from(history);

        return CommonResponse.success(response, "복약 여부가 업데이트되었습니다.");
    }
}