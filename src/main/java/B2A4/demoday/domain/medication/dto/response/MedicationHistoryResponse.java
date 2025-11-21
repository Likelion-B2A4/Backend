package B2A4.demoday.domain.medication.dto.response;

import B2A4.demoday.domain.medication.entity.MedicationHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MedicationHistoryResponse {
    private Long historyId;
    private Long recordId;
    private String date;
    private String period;
    private Boolean taken;
    private String takenAt;

    public static MedicationHistoryResponse from(MedicationHistory history) {
        return MedicationHistoryResponse.builder()
                .historyId(history.getId())
                .recordId(history.getMedicationRecord().getId())
                .date(history.getDate().toString())
                .period(history.getPeriod())
                .taken(history.getTaken())
                .takenAt(history.getTakenAt() != null ? history.getTakenAt().toString() : null)
                .build();
    }
}