package B2A4.demoday.domain.hospital.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HospitalNearbyResponse {
    private Long hospitalId;
    private String hospitalName;
    private Double latitude;
    private Double longitude;
}
