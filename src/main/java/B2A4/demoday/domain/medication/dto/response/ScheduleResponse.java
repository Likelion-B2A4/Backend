package B2A4.demoday.domain.medication.dto.response;

import B2A4.demoday.domain.medication.entity.MedicationSchedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@Builder
public class ScheduleResponse {
    private Long scheduleId;
    private String period;
    private String time;
    private Boolean enabled;

    public static ScheduleResponse from(MedicationSchedule schedule) {
        return ScheduleResponse.builder()
                .scheduleId(schedule.getId())
                .period(schedule.getPeriod())
                .time(schedule.getTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .enabled(schedule.getEnabled())
                .build();
    }
}