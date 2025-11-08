package B2A4.demoday.global.websocket;

import B2A4.demoday.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            String role = accessor.getFirstNativeHeader("role");

            log.info("[STOMP CONNECT] Headers: Authorization={}, role={}", authHeader, role);

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
            accessor.getSessionAttributes().put("role", userType);

            log.info("[인증성공] userId={}, role={}", userId, userType);
        }

        return message;
    }
}