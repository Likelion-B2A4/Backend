package B2A4.demoday.domain.hospital.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class HospitalSignupRequest {
    private String loginId;
    private String pwd;
    private String name;
    private List<String> specialties;
    private String address;
    private String contact;
    private Map<String, OperatingHourDto> operatingHours; // 요일별 운영시간
    private Map<String, BreakTimeDto> breakTimes; // 선택

    @JsonProperty(value = "image", required = false) // image -> imageUrl로 매핑
    private String imageUrl;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OperatingHourDto {
        private String openTime;
        private String closeTime;
        private boolean isClosed;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BreakTimeDto {
        private String breakStartTime;
        private String breakEndTime;
    }
}