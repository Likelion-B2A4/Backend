package B2A4.demoday.domain.doctor.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DoctorUpdateRequest {
    private String name;
    private String specialty;
    private String pinCode;

    @JsonProperty(value = "image", required = false)
    private String imageUrl;
}
