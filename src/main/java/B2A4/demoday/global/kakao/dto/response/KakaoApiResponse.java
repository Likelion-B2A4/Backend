package B2A4.demoday.global.kakao.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class KakaoApiResponse {
    private Meta meta;
    private List<Document> documents;

    @Data
    public static class Meta {
        @JsonProperty("total_count")
        private Integer totalCount;
    }

    @Data
    public static class Document {
        @JsonProperty("address_name")
        private String addressName;

        private String x; // 경도 (Longitude)
        private String y; // 위도 (Latitude)
    }
}
