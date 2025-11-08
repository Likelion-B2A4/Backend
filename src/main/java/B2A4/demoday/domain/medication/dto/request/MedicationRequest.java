package B2A4.demoday.domain.medication.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class MedicationRequest {
    private String name;
    private String startDate;
    private String endDate;
    private Boolean alarmEnabled;
    private List<String> daysOfWeek;
    private List<ScheduleRequest> schedules;
}