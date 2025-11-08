package B2A4.demoday.domain.hospital.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HospitalLoginRequest {
    private String loginId;
    private String pwd;
}