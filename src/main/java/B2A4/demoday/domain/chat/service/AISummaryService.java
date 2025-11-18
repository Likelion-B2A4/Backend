package B2A4.demoday.domain.chat.service;

import B2A4.demoday.domain.chat.dto.response.AISummaryResult;
import B2A4.demoday.domain.chat.entity.ChatMessage;
import B2A4.demoday.domain.chat.entity.ChatRoom;
import B2A4.demoday.domain.chat.repository.ChatMessageRepository;
import B2A4.demoday.domain.chat.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AISummaryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final WebClient webClient;

    @Async
    @Transactional
    public void generateSummaryAsync(Long chatRoomId) {
        try {
            log.info("[AI 요약 시작] chatRoomId={}", chatRoomId);

            // 1. 채팅 메시지 조회
            List<ChatMessage> messages =
                    chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);

            if (messages.isEmpty()) {
                log.warn("[AI 요약 중단] chatRoomId={} 메시지가 없습니다.", chatRoomId);
                return;
            }

            String conversationText = buildConversation(messages);

            AISummaryResult result = callOpenAiForSummary(conversationText);

            ChatRoom room = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new NoSuchElementException("채팅방을 찾을 수 없습니다."));

            room.updateSymptomSummary(result.getSymptomSummary());
            room.updateDiagnosisSummary(result.getDiagnosisSummary());

            log.info("[AI 요약 완료] chatRoomId={}", chatRoomId);

        } catch (Exception e) {
            log.error("[AI 요약 실패] chatRoomId={}, error={}", chatRoomId, e.getMessage(), e);
        }
    }

    private String buildConversation(List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage m : messages) {
            String who = "환자";
            if ("hospital".equals(m.getSenderType())) {
                who = "의사";
            }
            sb.append(who)
                    .append(": ")
                    .append(m.getContent())
                    .append("\n");
        }
        return sb.toString();
    }

    private AISummaryResult callOpenAiForSummary(String conversationText) {
        // 요청 바디용 내부 DTO
        record Message(String role, String content) {}

        record ChatRequest(
                String model,
                List<Message> messages
        ) {}

        record Choice(Message message) {}
        record ChatResponse(List<Choice> choices) {}

        String systemPrompt = """
            너는 의료 상담 채팅을 정리하는 비서야.
            아래 대화 내용을 보고 두 가지 요약을 JSON 형식의 문자열로 만들어줘.
            
            1) symptomSummary: 환자의 주요 증상, 경과, 관련 생활 습관 등을 요약 (한글, 1~2줄)
            2) diagnosisSummary: 의사의 진단, 처방, 추가 검사/주의사항 등을 요약 (한글, 2~4줄)
            
            반드시 다음 JSON 형식 **문자열만** 답해:
            {
              "symptomSummary": "...",
              "diagnosisSummary": "..."
            }
            다른 설명 없이 JSON만 출력해.
            """;

        Message system = new Message("system", systemPrompt);
        Message user = new Message("user", "다음은 의사와 환자의 채팅 기록입니다:\n\n" + conversationText);

        ChatRequest request = new ChatRequest(
                "gpt-4o-mini",
                List.of(system, user)
        );

        ChatResponse response = webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatResponse.class)
                .block();

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("OpenAI 응답이 비어 있습니다.");
        }

        String content = response.choices().get(0).message().content();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(content);

            String symptom = asTextOrEmpty(root, "symptomSummary");
            String diagnosis = asTextOrEmpty(root, "diagnosisSummary");

            return new AISummaryResult(symptom, diagnosis);
        } catch (Exception e) {
            log.error("AI 요약 JSON 파싱 실패, content={}", content, e);
            throw new RuntimeException("AI 요약 JSON 파싱 실패", e);
        }
    }

    private String asTextOrEmpty(JsonNode root, String field) {
        JsonNode node = root.get(field);
        return node != null && !node.isNull() ? node.asText() : "";
    }
}
