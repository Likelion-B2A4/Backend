package B2A4.demoday.domain.chat.service;

import B2A4.demoday.domain.chat.entity.ChatRoom;
import B2A4.demoday.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
class ChatRoomScheduler {
    private final ChatRoomRepository chatRoomRepository;
    private final AISummaryService summaryService;

    // 30분마다 실행
    @Scheduled(cron = "0 0/30 * * * *")
    @Transactional
    public void autoCloseExpiredChatRooms() {
        // 기준: 현재 시간으로부터 3시간 전
        LocalDateTime threshold = LocalDateTime.now().minusHours(3);

        // Status가 active이면서, startedAt이 3시간 전인 방 조회
        List<ChatRoom> expiredRooms = chatRoomRepository
                .findAllByStatusAndStartedAtBefore("active", threshold);

        for (ChatRoom room : expiredRooms) {
            log.info("[자동 종료] 진료 제한 시간(3시간) 초과로 종료. id={}, startedAt={}",
                    room.getId(), room.getStartedAt());

            // 종료 처리
            room.updateStatus("closed");
            room.updateFinishedAt(LocalDateTime.now());
            chatRoomRepository.save(room);

            summaryService.generateSummaryAsync(room.getId());
        }
    }

}
