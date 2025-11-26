package B2A4.demoday.global.websocket;

import B2A4.demoday.domain.chat.repository.ChatRoomRepository;
import B2A4.demoday.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final WebSocketSessionManager sessionManager;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor == null) {
                return message;
            }

            StompCommand command = accessor.getCommand();
            String sessionId = accessor.getSessionId();

            // 1. [CONNECT] 연결 시도 로그
            if (StompCommand.CONNECT.equals(command)) {
                String token = accessor.getFirstNativeHeader("Authorization");
                log.info("========== [WS CONNECT 시도] ==========");
                log.info("Session ID: {}", sessionId);
                log.info("Token 존재 여부: {}", (token != null && !token.isBlank()));

                // 여기서 에러나면 연결 거부됨
                handleConnect(accessor);
                log.info("[WS CONNECT 성공] 인증 완료");
            }

            // 2. [SUBSCRIBE] 구독 시도 로그
            else if (StompCommand.SUBSCRIBE.equals(command)) {
                String destination = accessor.getDestination();
                log.info("========== [WS SUBSCRIBE 시도] ==========");
                log.info("Session ID: {}", sessionId);
                log.info("구독 경로(Destination): {}", destination);

                handleSubscribe(accessor);
                log.info("[WS SUBSCRIBE 성공] 경로: {}", destination);
            }

            // 3. [UNSUBSCRIBE] 로그
            else if (StompCommand.UNSUBSCRIBE.equals(command)) {
                log.info("[WS UNSUBSCRIBE] Session ID: {}", sessionId);
                handleUnsubscribe(accessor);
            }

            return message;

        } catch (Exception e) {
            log.error("[WS 인터셉터 에러] 명령: {}, 원인: {}",
                    MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class).getCommand(),
                    e.getMessage(), e);

            return null; // 연결/요청 차단
        }
    }

    // CONNECT 처리 (JWT 토큰 검증 + 세션 속성 설정)
    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        log.info("[STOMP CONNECT] Authorization={}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Authorization 헤더 누락");
            throw new IllegalArgumentException("Missing Authorization header");
        }

        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("토큰 유효하지 않음");
            throw new IllegalArgumentException("Invalid JWT token");
        }

        Long userId = jwtTokenProvider.getUserId(token);
        String userType = jwtTokenProvider.getUserType(token);

        accessor.getSessionAttributes().put("userId", userId);
        accessor.getSessionAttributes().put("userType", userType);

        log.info("[인증성공] userId={}, userType={}", userId, userType);
    }

    // SUBSCRIBE 처리 (구독 경로 파싱 + 세션 추적 로그)
    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String userType = (String) accessor.getSessionAttributes().get("userType");

        if (destination == null || userId == null || userType == null) {
            log.warn("[STOMP SUBSCRIBE] destination 또는 사용자 정보 누락");
            return;
        }

        log.info("[STOMP SUBSCRIBE] userId={}, userType={}, destination={}", userId, userType, destination);

        // Pattern 1: /sub/doctors/{doctorId} (의사 대기 구독)
        Pattern doctorPattern = Pattern.compile("/sub/doctors/(\\d+)");
        Matcher doctorMatcher = doctorPattern.matcher(destination);
        if (doctorMatcher.matches()) {
            Long doctorId = Long.parseLong(doctorMatcher.group(1));

            boolean isHospital = "hospital".equals(userType); // 혹은 userType 확인

            if (!isHospital) {
                // 병원도 아니면 차단
                log.warn("[STOMP SUBSCRIBE] 권한 없는 구독 시도");
                throw new IllegalArgumentException("권한이 없습니다.");
            }

            // 세션 매니저에 연결 상태 등록
            String sessionId = accessor.getSessionId();
            sessionManager.connectDoctor(doctorId, sessionId);
            log.info("[의사 대기 구독] doctorId={}, sessionID={}", doctorId, sessionId);

            return;
        }

        // Pattern 2: /sub/chats/{roomId}/messages (채팅방 메시지 구독)
        Pattern roomPattern = Pattern.compile("/sub/chats/(\\d+)/messages");
        Matcher roomMatcher = roomPattern.matcher(destination);
        if (roomMatcher.matches()) {
            Long roomId = Long.parseLong(roomMatcher.group(1));
            
            // 세션 매니저에 연결 상태 등록
            sessionManager.connect(roomId, userType);
            log.info("[채팅방 구독] roomId={}, userId={}, userType={}", roomId, userId, userType);
            
            // 채팅방 상태 업데이트
            updateRoomStatusOnSubscribe(roomId, userType);
            
            return;
        }

        log.debug("[STOMP SUBSCRIBE] 처리되지 않은 경로: {}", destination);
    }
    
    // 구독 시 채팅방 상태 자동 업데이트
    private void updateRoomStatusOnSubscribe(Long roomId, String userType) {
        chatRoomRepository.findById(roomId).ifPresent(room -> {
            // 환자가 먼저 구독 → waiting
            if ("patient".equalsIgnoreCase(userType) && 
                "waiting".equalsIgnoreCase(room.getStatus())) {
                log.info("[채팅방 상태 유지] roomId={}, status=waiting (환자 구독)", roomId);
            }
            
            // 의사가 구독하고 환자도 이미 접속 중 → active
            if ("hospital".equalsIgnoreCase(userType) && 
                sessionManager.isPatientConnected(roomId)) {
                room.updateStatus("active");
                chatRoomRepository.save(room);
                log.info("[채팅방 상태 변경] roomId={}, status=active (양쪽 구독 완료)", roomId);
            }
            
            // 양쪽 모두 구독 완료 → active
            if (sessionManager.bothConnected(roomId) && 
                !"active".equalsIgnoreCase(room.getStatus())) {
                room.updateStatus("active");
                chatRoomRepository.save(room);
                log.info("[채팅방 상태 변경] roomId={}, status=active (동시 구독)", roomId);
            }
        });
    }

    // UNSUBSCRIBE 처리 (구독 해제 시 세션 추적 제거)
    private void handleUnsubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        String subscriptionId = accessor.getSubscriptionId();
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String userType = (String) accessor.getSessionAttributes().get("userType");

        log.info("[STOMP UNSUBSCRIBE] userId={}, userType={}, subscriptionId={}, destination={}", 
                userId, userType, subscriptionId, destination);

        if (destination == null) {
            log.warn("[STOMP UNSUBSCRIBE] destination 정보 누락. subscriptionId={}만으로 처리가 불가능한 로직.", subscriptionId);
            return;
        }

        Pattern doctorPattern = Pattern.compile("/sub/doctors/(\\d+)");
        Matcher doctorMatcher = doctorPattern.matcher(destination);
        if (doctorMatcher.matches()) {
            Long doctorId = Long.parseLong(doctorMatcher.group(1));

            // 세션 매니저에서 연결 상태 제거
            String sessionId = accessor.getSessionId();
            sessionManager.disconnectDoctor(doctorId);
            log.info("[의사 대기 구독 해제] doctorId={}, sessionID={}", doctorId, sessionId);

            return;
        }

        Pattern roomPattern = Pattern.compile("/sub/chats/(\\d+)/messages");
        Matcher roomMatcher = roomPattern.matcher(destination);
        if (roomMatcher.matches()) {
            Long roomId = Long.parseLong(roomMatcher.group(1));
            sessionManager.disconnect(roomId, userType);
            log.info("[채팅방 구독 해제] roomId={}, userId={}, userType={}", roomId, userId, userType);

            return;
        }

        log.debug("[STOMP UNSUBSCRIBE] 처리되지 않은 경로: {}", destination);
    }
}
