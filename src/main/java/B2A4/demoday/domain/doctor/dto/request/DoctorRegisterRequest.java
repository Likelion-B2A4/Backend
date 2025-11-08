package B2A4.demoday.domain.doctor.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class DoctorRegisterRequest {
    private String name;
    private String specialty;
    private String pinCode;

    @JsonProperty(value = "image", required = false) // image -> imageUrl로 매핑
    private String imageUrl;
}