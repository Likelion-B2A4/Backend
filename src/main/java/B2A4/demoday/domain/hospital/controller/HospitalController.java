package B2A4.demoday.domain.hospital.controller;

import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.hospital.dto.request.HospitalSignupRequest;
import B2A4.demoday.domain.hospital.dto.request.HospitalLoginRequest;
import B2A4.demoday.domain.hospital.dto.request.HospitalUpdateRequest;
import B2A4.demoday.domain.hospital.dto.response.HospitalSignupResponse;
import B2A4.demoday.domain.hospital.dto.response.HospitalLoginResponse;
import B2A4.demoday.domain.hospital.service.HospitalService;
import B2A4.demoday.global.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    // 병원 회원가입
    @PostMapping(value = "/signup", consumes = {"multipart/form-data"})
    public CommonResponse<HospitalSignupResponse> signup(
            @RequestPart("request") HospitalSignupRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return hospitalService.signup(request, image);
    }

    // 병원 로그인
    @PostMapping("/login")
    public CommonResponse<HospitalLoginResponse> login(@RequestBody HospitalLoginRequest request) {
        return hospitalService.login(request);
    }

    // 병원 로그아웃
    @PostMapping("/logout")
    public CommonResponse<Void> logout(HttpServletRequest request) {
        Long hospitalId = (Long) request.getAttribute(JwtAuthenticationFilter.ATTR_USER_ID);
        return hospitalService.logout(hospitalId);
    }

    // 병원 정보 수정
    @PatchMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<CommonResponse<HospitalSignupResponse>> update(
            HttpServletRequest httpRequest,
            @RequestPart("request") HospitalUpdateRequest requestDto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        Long hospitalId = (Long) httpRequest.getAttribute(JwtAuthenticationFilter.ATTR_USER_ID);
        CommonResponse<HospitalSignupResponse> response = hospitalService.update(hospitalId, requestDto, image);
        return ResponseEntity.ok(response);
    }
}