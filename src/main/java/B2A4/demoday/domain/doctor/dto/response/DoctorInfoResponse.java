package B2A4.demoday.domain.doctor.dto.response;

import B2A4.demoday.domain.doctor.entity.Doctor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DoctorInfoResponse {
    private Long id;
    private String name;
    private String specialty;

    public static DoctorInfoResponse from(Doctor doctor) {
        return DoctorInfoResponse.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .specialty(doctor.getSpecialty())
                .build();
    }
}