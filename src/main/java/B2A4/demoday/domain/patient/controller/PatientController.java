package B2A4.demoday.domain.patient.controller;

import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.patient.dto.request.PatientUpdateRequest;
import B2A4.demoday.domain.patient.dto.response.PatientLoginResponse;
import B2A4.demoday.domain.patient.dto.request.PatientLoginRequest;
import B2A4.demoday.domain.patient.dto.request.PatientSignupRequest;
import B2A4.demoday.domain.patient.dto.response.PatientSignupResponse;
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
}