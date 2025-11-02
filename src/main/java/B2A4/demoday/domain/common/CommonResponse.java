package B2A4.demoday.domain.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private boolean isSuccess;
    private String message;
    private T data;

    // 성공 응답 + 데이터 O
    public static <T> CommonResponse<T> success(T data, String message) {
        return new CommonResponse<>(true, message, data);
    }

    // 성공 응답 + 데이터 X
    public static <T> CommonResponse<T> success(String message) {
        return new CommonResponse<>(true, message, null);
    }

    // 실패 응답
    public static <T> CommonResponse<T> fail(String message) {
        return new CommonResponse<>(false, message, null);
    }
}
