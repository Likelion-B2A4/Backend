package B2A4.demoday.domain.doctor.dto.response;

import B2A4.demoday.domain.doctor.entity.Doctor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DoctorRegisterResponse {
    private Long doctorId;
    private Long hospitalId;
    private String hospitalName;
    private String name;
    private String specialty;
    private String imageUrl;
    private String qrCode;
    private LocalDateTime qrGeneratedAt;

    public static DoctorRegisterResponse from(Doctor doctor) {
        return DoctorRegisterResponse.builder()
                .doctorId(doctor.getId())
                .hospitalId(doctor.getHospital().getId())
                .hospitalName(doctor.getHospital().getName())
                .name(doctor.getName())
                .specialty(doctor.getSpecialty())
                .imageUrl(doctor.getImageUrl())
                .qrCode(doctor.getQrCode())
                .qrGeneratedAt(doctor.getQrGeneratedAt())
                .build();
    }
}