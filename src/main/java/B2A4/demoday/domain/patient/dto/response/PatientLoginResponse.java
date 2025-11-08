package B2A4.demoday.domain.patient.dto.response;

import B2A4.demoday.domain.patient.entity.Patient;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PatientLoginResponse {
    private String role;
    private Long patientId;
    private String loginId;
    private String name;
    private String accessToken;

    public static PatientLoginResponse from(Patient patient, String accessToken) {
        return PatientLoginResponse.builder()
                .role("patient")
                .patientId(patient.getId())
                .loginId(patient.getLoginId())
                .name(patient.getName())
                .accessToken(accessToken)
                .build();
    }
}