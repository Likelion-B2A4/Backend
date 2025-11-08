package B2A4.demoday.domain.doctor.dto.response;

import B2A4.demoday.domain.doctor.entity.Doctor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DoctorListResponse {
    private Long doctorId;
    private String name;
    private String specialty;
    private String imageUrl;
    private String lastTreatment;

    public static DoctorListResponse from(Doctor doctor) {
        return DoctorListResponse.builder()
                .doctorId(doctor.getId())
                .name(doctor.getName())
                .specialty(doctor.getSpecialty())
                .imageUrl(doctor.getImageUrl())
                .lastTreatment(doctor.getLastTreatment())
                .build();
    }
}