package B2A4.demoday.domain.medication.dto.response;

import B2A4.demoday.domain.medication.entity.MedicationRecord;
import B2A4.demoday.domain.medication.entity.MedicationSchedule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@Builder
public class MedicationResponse {
    private Long recordId;
    private String name;
    private String startDate;
    private String endDate;
    private Boolean alarmEnabled;
    private List<String> daysOfWeek;
    private List<ScheduleResponse> schedules;

    public static MedicationResponse from(MedicationRecord record) {
        ObjectMapper mapper = new ObjectMapper();
        List<String> daysOfWeek;
        try {
            daysOfWeek = mapper.readValue(record.getDaysOfWeek(), new TypeReference<>() {});
        } catch (Exception e) {
            daysOfWeek = List.of();
        }

        List<ScheduleResponse> scheduleResponses = record.getSchedules().stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());

        return MedicationResponse.builder()
                .recordId(record.getId())
                .name(record.getName())
                .startDate(record.getStartDate().format(DateTimeFormatter.ISO_DATE))
                .endDate(record.getEndDate().format(DateTimeFormatter.ISO_DATE))
                .alarmEnabled(record.getAlarmEnabled())
                .daysOfWeek(daysOfWeek)
                .schedules(scheduleResponses)
                .build();
    }
}