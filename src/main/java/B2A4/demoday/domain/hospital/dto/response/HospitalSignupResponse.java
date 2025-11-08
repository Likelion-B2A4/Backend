package B2A4.demoday.domain.hospital.dto.response;

import B2A4.demoday.domain.common.JsonUtil;
import B2A4.demoday.domain.hospital.entity.Hospital;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class HospitalSignupResponse {
    private Long hospitalId;
    private String loginId;
    private String name;
    private String address;
    private String contact;
    private List<String> specialties;
    private String imageUrl;
    private List<Map<String, Object>> operatingHours;
    private String updatedAt;

    public static HospitalSignupResponse from(Hospital hospital) {
        List<Map<String, Object>> hoursList = hospital.getOperatingHours().stream()
                .map(op -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("dayOfWeek", op.getDayOfWeek());
                    map.put("openTime", op.getOpenTime() != null ? op.getOpenTime().toString() : null);
                    map.put("closeTime", op.getCloseTime() != null ? op.getCloseTime().toString() : null);
                    map.put("breakStartTime", op.getBreakStartTime() != null ? op.getBreakStartTime().toString() : null);
                    map.put("breakEndTime", op.getBreakEndTime() != null ? op.getBreakEndTime().toString() : null);
                    map.put("isClosed", op.getIsClosed());
                    return map;
                })
                .collect(Collectors.toList());

        return HospitalSignupResponse.builder()
                .hospitalId(hospital.getId())
                .loginId(hospital.getLoginId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .contact(hospital.getContact())
                .specialties(JsonUtil.fromJson(hospital.getSpecialties()))
                .imageUrl(hospital.getImageUrl())
                .operatingHours(hoursList)
                .updatedAt(
                        hospital.getUpdatedAt() != null
                                ? hospital.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                : null
                )
                .build();
    }
}