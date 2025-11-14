package B2A4.demoday.domain.chat.service;

import B2A4.demoday.domain.chat.dto.request.ChatQrRequest;
import B2A4.demoday.domain.chat.dto.response.ChatCloseNotification;
import B2A4.demoday.domain.chat.dto.response.ChatMessageResponse;
import B2A4.demoday.domain.chat.dto.response.ChatRoomResponse;
import B2A4.demoday.domain.chat.dto.response.OriginalVoiceUrlResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final STTService sttService;

    // QR 스캔 시 채팅방 생성
    @Transactional
    public CommonResponse<ChatRoomResponse> scanQr(ChatQrRequest request, Long patientId) {
        String qrCode = request.getQrCode();

        // 의사 찾기
        Doctor doctor = doctorRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new NoSuchElementException("해당 QR 코드의 의사를 찾을 수 없습니다. QR 코드를 다시 확인해주세요."));

        // 환자 찾기
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NoSuchElementException("환자를 찾을 수 없습니다."));

        // 새 방 생성
        ChatRoom newRoom = ChatRoom.builder()
                .doctor(doctor)
                .patient(patient)
                .qrCode(qrCode)
                .status("waiting")
                .startedAt(LocalDateTime.now())
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(newRoom);
        ChatRoomResponse response = ChatRoomResponse.from(savedRoom);

        // 의사에게 Websocket 으로 새 채팅방 알림 전송
        messagingTemplate.convertAndSend("/sub/doctors/" + doctor.getId(), response);

        return CommonResponse.success(response, "채팅방 생성 성공");
    }

    // 진료 종료
    @Transactional
    public CommonResponse<?> closeChat(Long chatRoomId) {
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방을 찾을 수 없습니다."));

        room.updateStatus("closed");
        room.updateFinishedAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        // 양쪽에게 종료 알림 전송 (WebSocket 브로드캐스트)
        ChatCloseNotification closeNotification = ChatCloseNotification.create("진료가 종료되었습니다.");
        
        messagingTemplate.convertAndSend("/sub/chats/" + chatRoomId + "/messages", closeNotification);
        log.info("[진료 종료] chatRoomId={}, 종료 알림 전송 완료", chatRoomId);

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


    // WebSocket 통한 메시지 저장 (환자 전용 + 의사 긴급용)
    @Transactional
    public ChatMessage saveMessage(Long chatRoomId, String message, Long userId, String userType)
    {
        // 1. 채팅방 조회
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 2. 채팅방 상태 검증
        if ("closed".equalsIgnoreCase(room.getStatus())) {
            throw new IllegalStateException("종료된 채팅방에는 메시지를 보낼 수 없습니다.");
        }

        // 3. 권한 검증: 해당 채팅방의 참여자인지 확인
        if ("patient".equalsIgnoreCase(userType)) {
            if (room.getPatient() == null || !room.getPatient().getId().equals(userId)) {
                throw new AccessDeniedException("이 채팅방에 접근할 권한이 없습니다. (환자 불일치)");
            }
        } else if ("hospital".equalsIgnoreCase(userType)) {
            if (room.getDoctor() == null || !room.getDoctor().getId().equals(userId)) {
                throw new AccessDeniedException("이 채팅방에 접근할 권한이 없습니다. (의사 불일치)");
            }
        } else {
            throw new IllegalArgumentException("알 수 없는 사용자 타입입니다: " + userType);
        }

        // 4. 메시지 저장
        ChatMessage newMessage = ChatMessage.builder()
                .chatRoom(room)
                .senderType(userType)
                .senderId(userId)
                .messageType("text")
                .content(message)
                .originalAudioUrl(null)
                .build();

        return chatMessageRepository.save(newMessage);

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

    // 음성 파일 업로드 및 STT 변환 (의사 전용)
    @Transactional
    public CommonResponse<?> uploadVoiceMessage(Long chatRoomId, MultipartFile voiceFile, Long userId, String userType) {
        // 1. 권한 검증: 의사만 음성 전송 가능
        if (!"hospital".equalsIgnoreCase(userType)) {
            throw new AccessDeniedException("음성 메시지는 의사만 전송할 수 있습니다.");
        }

        // 2. 채팅방 조회
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방을 찾을 수 없습니다."));

        // 3. 의사 본인 확인
        if (!room.getDoctor().getId().equals(userId)) {
            throw new AccessDeniedException("해당 채팅방의 의사가 아닙니다.");
        }

        // 4. STT 변환
        log.info("[음성 메시지] 의사ID={}, 채팅방ID={}, 파일명={}", userId, chatRoomId, voiceFile.getOriginalFilename());
        String convertedText = sttService.convertToText(voiceFile);

        // 5. 메시지 저장
        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .senderType("hospital")
                .senderId(userId)
                .messageType("voice")
                .content(convertedText)
                .originalAudioUrl(voiceFile.getOriginalFilename()) // TODO: 실제 파일 저장 후 URL로 변경 (지금은 그냥 파일명)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 6. WebSocket 브로드캐스트
        ChatMessageResponse response = ChatMessageResponse.from(savedMessage);
        messagingTemplate.convertAndSend("/sub/chats/" + chatRoomId + "/messages", response);

        log.info("[음성 메시지 전송 완료] messageId={}, 변환된 텍스트={}", savedMessage.getId(), convertedText);

        return CommonResponse.success(response, "음성 메시지가 전송되었습니다.");
    }

    public CommonResponse<List<ChatMessageResponse>> getMessagesByChatRoomId(Long chatRoomId, Long userId, String userType) {
        // 1. 채팅방 찾기
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방을 찾을 수 없습니다."));

        // 2. userId 와 userType 이 속한 채팅방인지 검증
        if ("hospital".equals(userType) && !room.getDoctor().getId().equals(userId)) {
            throw new AccessDeniedException("해당 의사가 아닙니다.");
        }
        if ("patient".equals(userType) && !room.getPatient().getId().equals(userId)) {
            throw new AccessDeniedException("해당 환자가 아닙니다.");
        }

        // 3. 해당 채팅방의 List<ChatMessage> -> List<ChatMessageResponse>
        List<ChatMessageResponse> responses = room.getMessages().stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(ChatMessageResponse::from)
                .toList();

        return CommonResponse.success(responses, "채팅방 속 메시지 목록 조회 성공");
    }

    public CommonResponse<?> getOriginalVoiceMessage(Long messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("메시지를 찾을 수 없습니다."));

        OriginalVoiceUrlResponse response = OriginalVoiceUrlResponse.builder()
                .messageId(messageId)
                .messageType(message.getMessageType())
                .url(message.getOriginalAudioUrl())
                .build();

        return CommonResponse.success(response, "원본 파일 url 조회 성공");
    }
}