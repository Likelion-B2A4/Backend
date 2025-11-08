package B2A4.demoday.domain.hospital.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class HospitalUpdateRequest {
    private String name;
    private List<String> specialties;
    private String address;
    private String contact;
    private String pwd;          // 비밀번호 변경 요청 시 새 비밀번호
    private String currentPwd;   // 비밀번호 변경 시 기존 비밀번호 확인 필요하면 사용
    private Map<String, OperatingHourDto> operatingHours;
    private Map<String, BreakTimeDto> breakTimes;

    @JsonProperty("image") // image -> imageUrl로 매핑
    private String imageUrl;

    @Getter
    @NoArgsConstructor
    public static class OperatingHourDto {
        private String openTime;
        private String closeTime;
        private boolean isClosed;
    }

    @Getter
    @NoArgsConstructor
    public static class BreakTimeDto {
        private String breakStartTime;
        private String breakEndTime;
    }
}