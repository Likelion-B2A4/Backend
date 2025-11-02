package B2A4.demoday.global.exception;

import B2A4.demoday.domain.common.CommonResponse;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/* 사용 예시:

// IllegalArgumentException - 400
if (age < 0) {
    throw new IllegalArgumentException("나이는 0보다 작을 수 없습니다.");
}

// NoSuchElementException - 404
Hospital hospital = hospitalRepository.findById(id)
    .orElseThrow(() -> new NoSuchElementException("병원을 찾을 수 없습니다."));

*/

@RestControllerAdvice
public class GlobalExceptionHandler {

    // IllegalArgumentException - 잘못된 입력
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.fail(e.getMessage()));
    }

    // NoSuchElementException - 데이터 없음 (findById().orElseThrow() 등)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<CommonResponse<Void>> handleNoSuchElementException(NoSuchElementException e) {
        String message = e.getMessage() != null ? e.getMessage() : "요청한 데이터를 찾을 수 없습니다.";
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CommonResponse.fail(message));
    }

    // @Valid - 파라미터(컨트롤러 메서드 매개변수) 검증 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.fail("요청 파라미터가 올바르지 않습니다."));
    }

    // 필수 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.fail("필수 파라미터가 누락되었습니다: " + e.getParameterName()));
    }

    // 기타 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.fail("서버 오류가 발생했습니다."));
    }

}
