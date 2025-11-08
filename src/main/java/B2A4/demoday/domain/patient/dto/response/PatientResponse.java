package B2A4.demoday.domain.patient.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PatientResponse {
    private Long patientId;
    private String loginId;
    private String name;

    public static PatientResponse from(B2A4.demoday.domain.patient.entity.Patient p) {
        return PatientResponse.builder()
                .patientId(p.getId())
                .loginId(p.getLoginId())
                .name(p.getName())
                .build();
    }
}