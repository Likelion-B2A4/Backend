package B2A4.demoday.domain.chat.dto.response;

import B2A4.demoday.domain.chat.entity.ChatMessage;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VoiceMessageResponse {
    private Long messageId;
    private Long roomId;
    private String senderType;
    private String messageType;
    private String content;
    private String originalVoiceUrl;

    public static VoiceMessageResponse from(ChatMessage msg) {
        return VoiceMessageResponse.builder()
                .messageId(msg.getId())
                .roomId(msg.getChatRoom().getId())
                .senderType(msg.getSenderType())
                .messageType(msg.getMessageType())
                .content(msg.getContent())
                .build();
    }
}
