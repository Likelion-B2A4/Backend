package B2A4.demoday.domain.doctor.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DoctorSelectRequest {
    private Long doctorId;
    private String pinCode;
}