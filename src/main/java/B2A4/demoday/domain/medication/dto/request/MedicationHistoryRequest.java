package B2A4.demoday.domain.medication.dto.request;

import lombok.Data;

@Data
public class MedicationHistoryRequest {
    private String date;
    private String period;
    private Boolean taken;
}