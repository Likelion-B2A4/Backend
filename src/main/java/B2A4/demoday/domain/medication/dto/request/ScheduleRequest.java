package B2A4.demoday.domain.medication.dto.request;

import lombok.Data;

@Data
public class ScheduleRequest {
    private String period;
    private String time;
    private Boolean enabled;
}