package B2A4.demoday.domain.chat.dto.response;

import B2A4.demoday.domain.chat.entity.ChatRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomResponse {
    private Long chatRoomId;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String qrCode;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public static ChatRoomResponse from(ChatRoom room) {
        return ChatRoomResponse.builder()
                .chatRoomId(room.getId())
                .patientId(room.getPatient().getId())
                .patientName(room.getPatient().getName())
                .doctorId(room.getDoctor().getId())
                .doctorName(room.getDoctor().getName())
                .qrCode(room.getQrCode())
                .status(room.getStatus())
                .startedAt(room.getStartedAt())
                .finishedAt(room.getFinishedAt())
                .build();
    }
}