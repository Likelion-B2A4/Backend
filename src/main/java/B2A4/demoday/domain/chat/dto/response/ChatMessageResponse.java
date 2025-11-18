package B2A4.demoday.domain.chat.dto.response;

import B2A4.demoday.domain.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ChatMessageResponse {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String senderType;
    private String messageType;
    private String content;
    private String originalVoiceUrl;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .messageId(msg.getId())
                .roomId(msg.getChatRoom().getId())
                .senderId(msg.getSenderId())
                .senderType(msg.getSenderType())
                .messageType(msg.getMessageType())
                .content(msg.getContent())
                .originalVoiceUrl(msg.getOriginalAudioUrl())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}