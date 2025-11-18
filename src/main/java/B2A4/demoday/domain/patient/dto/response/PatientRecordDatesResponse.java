package B2A4.demoday.domain.patient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PatientRecordDatesResponse {
    private int year;
    private int month;
    private List<String> dates;
}