package B2A4.demoday.domain.chat.repository;

import B2A4.demoday.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoom_Id(Long chatRoomId);

    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}