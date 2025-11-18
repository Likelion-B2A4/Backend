package B2A4.demoday.domain.chat.dto.response;

import B2A4.demoday.domain.chat.entity.ChatRoom;
import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSummaryResponse {
    private boolean ready;
    private Long chatRoomId;
    private String symptomSummary;
    private String diagnosisSummary;

    public static ChatSummaryResponse ready(ChatRoom room) {
        return ChatSummaryResponse.builder()
                .ready(true)
                .chatRoomId(room.getId())
                .symptomSummary(room.getSymptomSummary())
                .diagnosisSummary(room.getDiagnosisSummary())
                .build();
    }

    public static ChatSummaryResponse pending(ChatRoom room) {
        return ChatSummaryResponse.builder()
                .ready(false)
                .chatRoomId(room.getId())
                .symptomSummary(room.getSymptomSummary())
                .diagnosisSummary(room.getDiagnosisSummary())
                .build();
    }
}
