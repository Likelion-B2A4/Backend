package B2A4.demoday.domain.doctor.controller;

import B2A4.demoday.domain.chat.dto.response.ChatRoomInfoResponse;
import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.doctor.dto.request.DoctorRegisterRequest;
import B2A4.demoday.domain.doctor.dto.request.DoctorSelectRequest;
import B2A4.demoday.domain.doctor.dto.request.DoctorUpdateRequest;
import B2A4.demoday.domain.doctor.dto.response.*;
import B2A4.demoday.domain.doctor.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    // 의사 등록
    @PostMapping(value = "/hospitals/doctors", consumes = {"multipart/form-data"})
    public CommonResponse<DoctorRegisterResponse> registerDoctor(
            @AuthenticationPrincipal Long hospitalId,
            @RequestPart("request") DoctorRegisterRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return doctorService.registerDoctor(hospitalId, request, image);
    }

    // 의사 정보 수정
    @PatchMapping(value = "/doctors/{doctorId}", consumes = {"multipart/form-data"})
    public CommonResponse<DoctorRegisterResponse> updateDoctor(
            @PathVariable Long doctorId,
            @RequestPart("request") DoctorUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return doctorService.updateDoctor(doctorId, request, image);
    }
    
    // 병원 소속 의사 목록 조회
    @GetMapping("/hospitals/doctors")
    public CommonResponse<List<DoctorListResponse>> getDoctorsByHospital(
            @AuthenticationPrincipal Long hospitalId) {
        return doctorService.getDoctorsByHospital(hospitalId);
    }

    // 의사 선택
    @PostMapping("/hospitals/doctors/select-doctor")
    public CommonResponse<DoctorSelectResponse> selectDoctor(@RequestBody DoctorSelectRequest request) {
        return doctorService.selectDoctor(request);
    }

    // QR 코드 조회 (태블릿용)
    @GetMapping("doctors/{doctorId}/qr")
    public CommonResponse<DoctorQrResponse> getDoctorQr(@PathVariable Long doctorId) {
        return doctorService.getDoctorQr(doctorId);
    }

    // QR 코드로 의사 정보 조회 (모바일용)
    @GetMapping("doctors/by-qr/{qrCode}")
    public CommonResponse<DoctorInfoResponse> getDoctorByQr(@PathVariable String qrCode) {
        return doctorService.getDoctorByQr(qrCode);
    }

    // QR 코드 재생성
    @PostMapping("doctors/{doctorId}/regenerate-qr")
    public CommonResponse<DoctorQrRegenerateResponse> regenerateDoctorQr(@PathVariable Long doctorId) {
        return doctorService.regenerateDoctorQr(doctorId);
    }

    // 채팅방 목록 조회
    @GetMapping("doctors/{doctorId}/chats")
    public CommonResponse<List<ChatRoomInfoResponse>> getChatRoomList(@PathVariable Long doctorId) {
        return doctorService.getMyChatRoomList(doctorId);
    }
}