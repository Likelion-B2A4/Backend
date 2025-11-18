package B2A4.demoday.domain.chat.controller;

import B2A4.demoday.domain.chat.dto.request.ChatMessageRequest;
import B2A4.demoday.domain.chat.dto.response.ChatMessageResponse;
import B2A4.demoday.domain.chat.entity.ChatMessage;
import B2A4.demoday.domain.chat.service.ChatService;
import B2A4.demoday.global.websocket.WebSocketSessionManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;  // ✅ 추가
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;

    // /pub/chats/{chatRoomId}/send 경로로 메시지 전송 시 호출되는 메서드
    @MessageMapping("/chats/{chatRoomId}/send")
    public void sendChatMessage(@DestinationVariable Long chatRoomId,
                                @Payload ChatMessageRequest request,
                                SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String userType = (String) accessor.getSessionAttributes().get("userType");

        if (userId == null || userType == null) {
            log.error("인증 실패: 세션 정보 없음");
            return;
        }

        try {
            // 메시지 저장
            ChatMessage saved = chatService.saveMessage(chatRoomId, request.getMessage(), userId, userType);

            // 변환 후 브로드캐스트
            ChatMessageResponse response = ChatMessageResponse.from(saved);
            messagingTemplate.convertAndSend("/sub/chats/" + chatRoomId + "/messages", response);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }


//    @MessageMapping("/chat.sendMessage")
//    public void sendMessage(ChatMessage message, StompHeaderAccessor accessor) {
//        Long userId = (Long) accessor.getSessionAttributes().get("userId");
//        String userType = (String) accessor.getSessionAttributes().get("userType");
//
//        if (userId == null || userType == null) {
//            log.error("인증 실패: 세션 정보 없음 (userId/userType)");
//            return;
//        }
//
//        Long roomId = (message.getChatRoom() != null) ? message.getChatRoom().getId() : null;
//        if (roomId == null) {
//            log.warn("잘못된 메시지 요청: roomId 없음");
//            return;
//        }
//
//        if (!sessionManager.bothConnected(roomId)) {
//            messagingTemplate.convertAndSend(
//                    "/sub/chat.room." + roomId,
//                    Map.of("system", "상대방이 아직 접속하지 않았습니다. 잠시만 기다려주세요.")
//            );
//            return;
//        }
//
//        // 메시지 저장
//        ChatMessage saved = chatService.saveMessage(message, userId);
//
//        // DTO 변환 후 전송
//        ChatMessageResponse response = ChatMessageResponse.from(saved);
//        messagingTemplate.convertAndSend("/sub/chat.room." + roomId, response);
//
//        log.info("[{}] {} → room {}", userType, userId, roomId);
//    }
}