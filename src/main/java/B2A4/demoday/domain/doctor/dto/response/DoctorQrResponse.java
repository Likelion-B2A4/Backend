package B2A4.demoday.domain.doctor.dto.response;

import B2A4.demoday.domain.doctor.entity.Doctor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DoctorQrResponse {
    private String qr;
    private String doctorName;
    private String hospitalName;
    private String imageUrl;
    private String specialty;
    private LocalDateTime qrGeneratedAt;

    public static DoctorQrResponse from(Doctor doctor) {
        return DoctorQrResponse.builder()
                .qr(doctor.getQrCode())
                .doctorName(doctor.getName())
                .hospitalName(doctor.getHospital().getName())
                .imageUrl(doctor.getImageUrl())
                .specialty(doctor.getSpecialty())
                .qrGeneratedAt(doctor.getQrGeneratedAt())
                .build();
    }
}