package B2A4.demoday.domain.chat.controller;

import B2A4.demoday.domain.chat.dto.request.ChatQrRequest;
import B2A4.demoday.domain.chat.dto.response.ChatMessageResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 채팅방 생성
    @PostMapping("/rooms/scan-qr")
    @Transactional
    public CommonResponse<ChatRoomResponse> scanQr(
            @RequestBody ChatQrRequest request,
            HttpServletRequest httpRequest
    ) {

        Long patientId = (Long) httpRequest.getAttribute(JwtAuthenticationFilter.ATTR_USER_ID);
        return chatService.scanQr(request, patientId);

    }

    // 진료 종료
    @PostMapping("/{chatRoomId}/close")
    public CommonResponse<?> closeChat(@PathVariable Long chatRoomId) {
        return chatService.closeChat(chatRoomId);
    }

    // 음성 파일 업로드 (의사 전용)
    @PostMapping("/{chatRoomId}/messages/voice")
    public CommonResponse<?> uploadVoice(
            @PathVariable Long chatRoomId,
            @RequestParam("voice") MultipartFile voiceFile,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute(JwtAuthenticationFilter.ATTR_USER_ID);
        String userType = (String) httpRequest.getAttribute(JwtAuthenticationFilter.ATTR_USER_TYPE);

        return chatService.uploadVoiceMessage(chatRoomId, voiceFile, userId, userType);
    }

    // 채팅방 메시지 목록 조회
    @GetMapping("/{chatRoomId}/messages")
    public CommonResponse<List<ChatMessageResponse>> getMessages(
            @PathVariable Long chatRoomId,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute(JwtAuthenticationFilter.ATTR_USER_ID);
        String userType = (String) httpRequest.getAttribute(JwtAuthenticationFilter.ATTR_USER_TYPE);

        return chatService.getMessagesByChatRoomId(chatRoomId, userId, userType);
    }

    // 원본 미디어 조회


}
