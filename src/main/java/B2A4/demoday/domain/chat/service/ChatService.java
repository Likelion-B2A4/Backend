package B2A4.demoday.domain.chat.service;

import B2A4.demoday.domain.chat.dto.request.ChatQrRequest;
import B2A4.demoday.domain.chat.dto.response.ChatRoomResponse;
import B2A4.demoday.domain.chat.entity.ChatMessage;
import B2A4.demoday.domain.chat.entity.ChatRoom;
import B2A4.demoday.domain.chat.repository.ChatMessageRepository;
import B2A4.demoday.domain.chat.repository.ChatRoomRepository;
import B2A4.demoday.domain.common.CommonResponse;
import B2A4.demoday.domain.doctor.entity.Doctor;
import B2A4.demoday.domain.patient.entity.Patient;
import B2A4.demoday.domain.doctor.repository.DoctorRepository;
import B2A4.demoday.domain.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    // QR 스캔 시 채팅방 생성
    @Transactional
    public CommonResponse<ChatRoomResponse> scanQr(ChatQrRequest request, Long patientId) {
        String qrCode = request.getQrCode();

        // 의사 찾기
        Doctor doctor = doctorRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new NoSuchElementException("유효하지 않은 QR 코드입니다."));

        // 환자 찾기
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NoSuchElementException("환자를 찾을 수 없습니다."));

        // 기존 closed 방이 있으면 삭제 후 새로 생성
        chatRoomRepository.findByQrCode(qrCode).ifPresent(existing -> {
            if (!"active".equalsIgnoreCase(existing.getStatus())) {
                chatRoomRepository.delete(existing);
            }
        });

        // 새 방 생성
        ChatRoom newRoom = ChatRoom.builder()
                .doctor(doctor)
                .patient(patient)
                .qrCode(qrCode)
                .status("waiting")
                .startedAt(LocalDateTime.now())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(newRoom);
        return CommonResponse.success(ChatRoomResponse.from(savedRoom), "채팅방 생성 성공");
    }

    // 진료 종료
    @Transactional
    public CommonResponse<?> closeChat(Long chatRoomId) {
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방을 찾을 수 없습니다."));

        room.updateStatus("closed");
        room.updateFinishedAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        // 엔티티 대신 DTO로 변환해서 반환
        return CommonResponse.success(
                ChatRoomResponse.from(room),
                "진료가 종료되었습니다. AI 요약이 자동으로 생성됩니다."
        );
    }

    // 외부에서 ChatRoom을 직접 조회할 때 사용 (Controller 편의용)
    public ChatRoom getChatRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
    }

    // 메시지 저장 (WebSocket 통해 호출됨)
    @Transactional
    public ChatMessage saveMessage(ChatMessage message, Long userId) {
        // userType 가져오기 (JWT에서)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userType = (auth != null && auth.getDetails() instanceof Map<?, ?> details)
                ? (String) details.get("userType") : null;

        ChatRoom room = chatRoomRepository.findById(message.getChatRoom().getId())
                .orElseThrow(() -> new RuntimeException("채팅방 없음"));

        // 권한 검증
        if ("hospital".equals(userType) && !room.getDoctor().getId().equals(userId)) {
            throw new AccessDeniedException("해당 의사가 아닙니다.");
        }
        if ("patient".equals(userType) && !room.getPatient().getId().equals(userId)) {
            throw new AccessDeniedException("해당 환자가 아닙니다.");
        }

        message.setSenderId(userId);
        message.setChatRoom(room);
        return chatMessageRepository.save(message);
    }
}