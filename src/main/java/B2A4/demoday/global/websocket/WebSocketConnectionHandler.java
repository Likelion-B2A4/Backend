package B2A4.demoday.global.websocket;

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

    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        return new WebSocketHandlerDecorator(handler) {

            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                String sessionId = session.getId();
                log.info("[WebSocket 연결] sessionId={}", sessionId);
                
                super.afterConnectionEstablished(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                String sessionId = session.getId();
                log.info("[WebSocket 연결 해제] sessionId={}, status={}", sessionId, closeStatus);
                
                super.afterConnectionClosed(session, closeStatus);
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                log.error("[WebSocket 전송 오류] sessionId={}, error={}", session.getId(), exception.getMessage());
                super.handleTransportError(session, exception);
            }
        };
    }
}
