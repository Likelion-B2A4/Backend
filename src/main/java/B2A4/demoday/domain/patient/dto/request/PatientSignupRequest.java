package B2A4.demoday.domain.patient.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PatientSignupRequest {
    private String loginId;
    private String pwd;
    private String name;
}
