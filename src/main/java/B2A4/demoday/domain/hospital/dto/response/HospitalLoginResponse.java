package B2A4.demoday.domain.hospital.dto.response;

import B2A4.demoday.domain.hospital.entity.Hospital;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HospitalLoginResponse {
    private String role;
    private Long hospitalId;
    private String loginId;
    private String name;
    private String accessToken;
    private String imageUrl;

    public static HospitalLoginResponse from(Hospital hospital, String accessToken) {
        return HospitalLoginResponse.builder()
                .role("hospital")
                .hospitalId(hospital.getId())
                .loginId(hospital.getLoginId())
                .name(hospital.getName())
                .accessToken(accessToken)
                .imageUrl(hospital.getImageUrl())
                .build();
    }
}