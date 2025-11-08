package B2A4.demoday.domain.doctor.dto.response;

import B2A4.demoday.domain.doctor.entity.Doctor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DoctorQrRegenerateResponse {
    private String qr;
    private Long doctorId;
    private String previousQr;
    private LocalDateTime qrGeneratedAt;

    public static DoctorQrRegenerateResponse from(Doctor doctor, String previousQr) {
        return DoctorQrRegenerateResponse.builder()
                .qr(doctor.getQrCode())
                .doctorId(doctor.getId())
                .previousQr(previousQr)
                .qrGeneratedAt(doctor.getQrGeneratedAt())
                .build();
    }
}