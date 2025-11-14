package B2A4.demoday.domain.chat.dto.response;

import B2A4.demoday.domain.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChatMessageResponse {
    private Long messageId;
    private Long roomId;
    private String senderType;
    private String messageType;
    private String content;

    public static ChatMessageResponse from(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .messageId(msg.getId())
                .roomId(msg.getChatRoom().getId())
                .senderType(msg.getSenderType())
                .messageType(msg.getMessageType())
                .content(msg.getContent())
                .build();
    }
}