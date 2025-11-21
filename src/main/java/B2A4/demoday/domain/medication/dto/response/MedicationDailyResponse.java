package B2A4.demoday.domain.medication.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicationDailyResponse {
    private Long recordId;
    private String name;
    private String period;
    private String time;
    private Boolean taken;
}