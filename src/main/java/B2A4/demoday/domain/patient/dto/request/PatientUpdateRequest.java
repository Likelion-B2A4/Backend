package B2A4.demoday.domain.patient.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PatientUpdateRequest {
    // 원하는 것만 보낼 수 있도록 모두 nullable 처리
    private String name;
    private String pwd;         // 비밀번호 변경 요청 시 새 비밀번호
}