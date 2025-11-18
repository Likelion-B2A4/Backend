package B2A4.demoday.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ChatCloseNotification {
    private String type;        // "system"
    private String action;      // "closed"
    private String content;     // 메시지 내용
    private LocalDateTime timestamp;

    public static ChatCloseNotification create(String content) {
        return ChatCloseNotification.builder()
                .type("system")
                .action("closed")
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
