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
    public void autoCloseExpiredChatRooms() {
        // 기준: 현재 시간으로부터 3시간 전
        LocalDateTime threshold = LocalDateTime.now().minusHours(3);

        // Status가 active이면서, startedAt이 3시간 전인 방 조회
        List<ChatRoom> expiredRooms = chatRoomRepository
                .findAllByStatusAndStartedAtBefore("active", threshold);

        if (expiredRooms.isEmpty()) {
            return;
        }

        log.info("[자동 종료 스케줄러] {}개의 만료된 채팅방 발견", expiredRooms.size());

        for (ChatRoom room : expiredRooms) {
            try {
                processAutoClose(room);
            } catch (Exception e) {
                log.error("[자동 종료 실패] chatRoomId={} 처리 중 오류 발생", room.getId(), e);
            }
        }
    }

    private void processAutoClose(ChatRoom room) {
        log.info("[자동 종료 진행] id={}, startedAt={}", room.getId(), room.getStartedAt());

        room.updateStatus("closed");
        room.updateFinishedAt(LocalDateTime.now());

        // 즉시 커밋으로 수정
        chatRoomRepository.saveAndFlush(room);

        summaryService.generateSummaryAsync(room.getId());
    }

}
