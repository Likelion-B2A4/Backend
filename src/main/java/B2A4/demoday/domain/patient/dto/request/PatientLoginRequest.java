package B2A4.demoday.domain.patient.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PatientLoginRequest {
    private String loginId;
    private String pwd;
}
