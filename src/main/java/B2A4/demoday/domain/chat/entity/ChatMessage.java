package B2A4.demoday.domain.chat.entity;

import B2A4.demoday.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ChatMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    // patient/hospital
    @Column(name = "sender_type", nullable = false)
    private String senderType;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    // text/sign_message/voice
    @Column(name = "message_type")
    private String messageType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "original_video_url")
    private String originalVideoUrl;

    @Column(name = "original_audio_url")
    private String originalAudioUrl;

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
}
