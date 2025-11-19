package B2A4.demoday.global.openai.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatResponse {
    private List<Choice> choices;

    @Data
    @Builder
    public static class Choice {
        private Message message;
    }

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}
