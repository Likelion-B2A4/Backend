package B2A4.demoday.domain.chat.controller;

import B2A4.demoday.domain.chat.dto.request.ChatQrRequest;
import B2A4.demoday.domain.chat.dto.response.ChatRoomResponse;
import B2A4.demoday.domain.chat.entity.ChatRoom;
import B2A4.demoday.domain.chat.repository.ChatRoomRepository;
import B2A4.demoday.domain.chat.service.ChatService;
import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.doctor.entity.Doctor;
import B2A4.demoday.domain.doctor.repository.DoctorRepository;
import B2A4.demoday.domain.patient.entity.Patient;
import B2A4.demoday.domain.patient.repository.PatientRepository;
import B2A4.demoday.global.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ChatRoomRepository chatRoomRepository;

    @PostMapping("/rooms/scan-qr")
    @Transactional
    public CommonResponse<ChatRoomResponse> scanQr(
            @RequestBody ChatQrRequest request,
            HttpServletRequest httpRequest
    ) {
        Long patientId = (Long) httpRequest.getAttribute(JwtAuthenticationFilter.ATTR_USER_ID);

        Doctor doctor = doctorRepository.findByQrCode(request.getQrCode())
                .orElseThrow(() -> new NoSuchElementException("해당 QR코드의 의사를 찾을 수 없습니다."));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NoSuchElementException("환자를 찾을 수 없습니다."));

        ChatRoom newRoom = ChatRoom.builder()
                .doctor(doctor)
                .patient(patient)
                .qrCode(request.getQrCode())
                .status("waiting")
                .startedAt(LocalDateTime.now())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(newRoom);

        // 무한루프 방지: 엔티티 대신 DTO로 반환
        return CommonResponse.success(
                ChatRoomResponse.from(savedRoom),
                "채팅방 생성 성공"
        );
    }

    // 진료 종료
    @PostMapping("/{chatRoomId}/close")
    public CommonResponse<?> closeChat(@PathVariable Long chatRoomId) {
        return chatService.closeChat(chatRoomId);
    }
}

@Getter
class QrScanRequest {
    private String qrCode;
}