package B2A4.demoday.domain.patient.controller;

import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.patient.dto.request.PatientUpdateRequest;
import B2A4.demoday.domain.patient.dto.response.PatientLoginResponse;
import B2A4.demoday.domain.patient.dto.request.PatientLoginRequest;
import B2A4.demoday.domain.patient.dto.request.PatientSignupRequest;
import B2A4.demoday.domain.patient.dto.response.PatientRecordDatesResponse;
import B2A4.demoday.domain.patient.dto.response.PatientRecordDetailResponse;
import B2A4.demoday.domain.patient.dto.response.PatientSignupResponse;
import B2A4.demoday.domain.patient.service.PatientRecordService;
import B2A4.demoday.domain.patient.service.PatientService;
import B2A4.demoday.global.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final PatientRecordService patientRecordService;

    // 환자 회원가입
    @PostMapping("/signup")
    public CommonResponse<PatientSignupResponse> signup(@RequestBody PatientSignupRequest request) {
        return patientService.signup(request);
    }

    // 환자 로그인
    @PostMapping("/login")
    public CommonResponse<PatientLoginResponse> login(@RequestBody PatientLoginRequest request) {
        return patientService.login(request);
    }

    // 환자 로그아웃
    @PostMapping("/logout")
    public CommonResponse<Void> logout(HttpServletRequest request) {
        Long patientId = (Long) request.getAttribute("userId");
        return patientService.logout(patientId);
    }

    // 환자 정보 수정
    @PatchMapping
    public ResponseEntity<CommonResponse<PatientSignupResponse>> updatePatient(
            HttpServletRequest request,
            @RequestBody PatientUpdateRequest updateRequest
    ) {
        Long userId = (Long) request.getAttribute(JwtAuthenticationFilter.ATTR_USER_ID);
        CommonResponse<PatientSignupResponse> response = patientService.update(userId, updateRequest);
        return ResponseEntity.ok(response);
    }

    // 특정 달 진료이력 날짜 목록 조회
    @GetMapping("/records/dates")
    public CommonResponse<PatientRecordDatesResponse> getRecordDates(
            HttpServletRequest request,
            @RequestParam int year,
            @RequestParam int month
    ) {
        Long patientId = (Long) request.getAttribute(JwtAuthenticationFilter.ATTR_USER_ID);
        return patientRecordService.getRecordDates(patientId, year, month);
    }

    // 특정 날짜 진료이력 상세 조회
    @GetMapping("/records")
    public CommonResponse<PatientRecordDetailResponse> getRecordDetail(
            HttpServletRequest request,
            @RequestParam String date
    ) {
        Long patientId = (Long) request.getAttribute(JwtAuthenticationFilter.ATTR_USER_ID);
        return patientRecordService.getRecordDetail(patientId, date);
    }
}