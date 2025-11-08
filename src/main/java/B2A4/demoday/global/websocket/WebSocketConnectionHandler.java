package B2A4.demoday.global.websocket;

import B2A4.demoday.domain.chat.repository.ChatRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketConnectionHandler implements WebSocketHandlerDecoratorFactory {

    private final WebSocketSessionManager sessionManager;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        return new WebSocketHandlerDecorator(handler) {

            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {

                // 예: ?roomId=1&role=patient
                String query = session.getUri().getQuery();
                Long roomId = extractQueryValue(query, "roomId");
                String role = extractQueryString(query, "role");

                // 현재 세션 등록
                sessionManager.connect(roomId, role);
                log.info("Connected → roomId={}, role={}", roomId, role);

                // 환자가 먼저 들어온 경우 → waiting
                if ("patient".equalsIgnoreCase(role)) {
                    updateRoomStatus(roomId, "waiting");
                    log.info("Room {} waiting (patient connected first)", roomId);
                }

                // 의사가 들어왔고 환자가 이미 접속 중이라면 → active
                if ("hospital".equalsIgnoreCase(role)
                        && sessionManager.isPatientConnected(roomId)) {
                    updateRoomStatus(roomId, "active");
                    log.info("Room {} active (doctor joined after patient)", roomId);
                }

                // (예외) 두 명이 동시에 들어올 경우도 active로 처리
                if (sessionManager.bothConnected(roomId)) {
                    updateRoomStatus(roomId, "active");
                    log.info("Room {} active (both connected simultaneously)", roomId);
                }

                super.afterConnectionEstablished(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                String query = session.getUri().getQuery();
                Long roomId = extractQueryValue(query, "roomId");
                String role = extractQueryString(query, "role");

                sessionManager.disconnect(roomId, role);
                log.info("Disconnected → roomId={}, role={}", roomId, role);

                // 누가 나가든 closed 처리
                updateRoomStatus(roomId, "closed");

                super.afterConnectionClosed(session, closeStatus);
            }
        };
    }

    @Transactional
    protected void updateRoomStatus(Long roomId, String status) {
        chatRoomRepository.findById(roomId).ifPresent(room -> {
            room.updateStatus(status);
            chatRoomRepository.save(room);
            log.info("Room {} status changed to {}", roomId, status);
        });
    }

    // --- 파라미터 파싱 보강 ---
    private Long extractQueryValue(String query, String key) {
        try {
            if (query == null) return null;
            for (String part : query.split("&")) {
                if (part.startsWith(key + "=")) {
                    return Long.parseLong(part.split("=")[1]);
                }
            }
        } catch (Exception e) {
            log.warn("roomId 파싱 실패: {}", e.getMessage());
        }
        return null;
    }

    private String extractQueryString(String query, String key) {
        try {
            if (query == null) return null;
            for (String part : query.split("&")) {
                if (part.startsWith(key + "=")) {
                    return part.split("=")[1];
                }
            }
        } catch (Exception e) {
            log.warn("role 파싱 실패: {}", e.getMessage());
        }
        return null;
    }
}