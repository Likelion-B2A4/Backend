package B2A4.demoday.domain.medication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class MedicationMonthlyResponse {
    private Long recordId;
    private String name;
    private List<DayStatus> days;

    @Data
    @AllArgsConstructor
    public static class DayStatus {
        private String date;
        private Boolean taken;
    }
}