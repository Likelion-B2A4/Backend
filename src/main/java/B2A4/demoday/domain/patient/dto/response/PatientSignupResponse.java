package B2A4.demoday.domain.patient.dto.response;

import B2A4.demoday.domain.patient.entity.Patient;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PatientSignupResponse {
    private Long patientId;
    private String loginId;
    private String name;

    public static PatientSignupResponse from(Patient patient) {
        return PatientSignupResponse.builder()
                .patientId(patient.getId())
                .loginId(patient.getLoginId())
                .name(patient.getName())
                .build();
    }
}
