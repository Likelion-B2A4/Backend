package B2A4.demoday.domain.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/* 사용 예시:

// 저장 시
List<String> specialties = Arrays.asList("내과", "소화기내과", "당뇨");
hospital.setSpecialties(JsonUtil.toJson(specialties));

// 조회 시
List<String> specialtyList = JsonUtil.fromJson(hospital.getSpecialties());
// ["내과", "소화기내과", "당뇨"]

*/

/** JSON 변환 유틸리티 클래스 **/
@Slf4j
public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** List<String> -> JSON 문자열 **/
    public static String toJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert list to JSON", e);
            return null;
        }
    }

    /** JSON 문자열 -> List<String> **/
    public static List<String> fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JSON to list", e);
            return new ArrayList<>();
        }
    }
}
