package B2A4.demoday.domain.doctor.dto.response;

import B2A4.demoday.domain.doctor.entity.Doctor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DoctorSelectResponse {
    private Long hospitalId;
    private String hospitalName;
    private Long doctorId;
    private String doctorName;
    private String specialty;
    private String imageUrl;

    public static DoctorSelectResponse from(Doctor doctor) {
        return DoctorSelectResponse.builder()
                .hospitalId(doctor.getHospital().getId())
                .hospitalName(doctor.getHospital().getName())
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .specialty(doctor.getSpecialty())
                .imageUrl(doctor.getImageUrl())
                .build();
    }
}