package B2A4.demoday.domain.medication.controller;

import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.medication.dto.request.MedicationHistoryRequest;
import B2A4.demoday.domain.medication.dto.request.MedicationRequest;
import B2A4.demoday.domain.medication.dto.response.MedicationDailyResponse;
import B2A4.demoday.domain.medication.dto.response.MedicationHistoryResponse;
import B2A4.demoday.domain.medication.dto.response.MedicationResponse;
import B2A4.demoday.domain.medication.service.MedicationHistoryService;
import B2A4.demoday.domain.medication.service.MedicationService;
import B2A4.demoday.global.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/patients/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;
    private final MedicationHistoryService historyService;

    // 복약 일정 추가
    @PostMapping
    public CommonResponse<MedicationResponse> createMedication(
            @AuthenticationPrincipal Long patientId,
            @RequestBody MedicationRequest request) {

        MedicationResponse response = medicationService.createMedication(patientId, request);
        return CommonResponse.success(response, "복약 일정이 추가되었습니다.");
    }

    // 복약 일정 수정 (원하는 항목만)
    @PatchMapping("/{recordId}")
    public CommonResponse<MedicationResponse> updateMedication(
            @PathVariable Long recordId,
            @RequestBody MedicationRequest request
    ) {
        return medicationService.updateMedication(recordId, request);
    }

    // 복용 여부 업데이트
    @PatchMapping("/{recordId}/history")
    public CommonResponse<MedicationHistoryResponse> updateHistory(
            @AuthenticationPrincipal Long patientId,
            @PathVariable Long recordId,
            @RequestBody MedicationHistoryRequest request
    ) {
        return historyService.updateHistory(patientId, recordId, request);
    }
    
    // 복약 일정 조회
    @GetMapping
    public CommonResponse<List<MedicationResponse>> getMedications(
            @AuthenticationPrincipal Long patientId,
            @RequestParam(required = false) String date) {

        List<MedicationResponse> response = medicationService.getMedications(patientId, date);
        return CommonResponse.success(response, "복약 일정 조회 성공");
    }


    // 복약 일정 조회(특정 날짜)
    @GetMapping("/daily")
    public CommonResponse<List<MedicationDailyResponse>> getMedicationsByDate(
            @AuthenticationPrincipal Long patientId,
            @RequestParam String date
    ) {
        List<MedicationDailyResponse> response =
                medicationService.getDailyMedications(patientId, date);

        return CommonResponse.success(response, "특정 날짜 복약 조회 성공");
    }


    // 복약 일정 삭제(record 삭제)
    @DeleteMapping("/{recordId}")
    public CommonResponse<Void> deleteMedication(
            @AuthenticationPrincipal Long patientId,
            @PathVariable Long recordId) {

        medicationService.deleteMedication(patientId, recordId);
        return CommonResponse.success(null, "복약 일정이 삭제되었습니다.");
    }

    // 복약 일정 삭제(해당 날짜)
    @DeleteMapping("/{recordId}/single")
    public CommonResponse<Void> deleteSingleDay(
            @AuthenticationPrincipal Long patientId,
            @PathVariable Long recordId,
            @RequestParam String date
    ) {
        medicationService.deleteMedicationByDate(
                patientId,
                recordId,
                date,
                "single"
        );

        return CommonResponse.success(null, "해당 날짜 복약 일정이 삭제되었습니다.");
    }

    // 복약 일정 삭제(해당 날짜 포함 이후까지)
    @DeleteMapping("/{recordId}/after")
    public CommonResponse<Void> deleteAfterDay(
            @AuthenticationPrincipal Long patientId,
            @PathVariable Long recordId,
            @RequestParam String date
    ) {
        medicationService.deleteMedicationByDate(
                patientId,
                recordId,
                date,
                "after"
        );

        return CommonResponse.success(null, "해당 날짜 포함 이후 복약 일정이 삭제되었습니다.");
    }
}