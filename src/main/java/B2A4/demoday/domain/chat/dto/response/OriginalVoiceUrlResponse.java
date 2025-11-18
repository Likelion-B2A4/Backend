package B2A4.demoday.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OriginalVoiceUrlResponse {
    private Long messageId;
    private String fileName;
    private String url;
    private String messageType;
}
