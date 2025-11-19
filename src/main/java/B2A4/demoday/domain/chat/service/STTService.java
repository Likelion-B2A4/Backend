package B2A4.demoday.domain.chat.service;

import B2A4.demoday.global.openai.dto.request.ChatRequest;
import B2A4.demoday.global.openai.dto.response.ChatResponse;
import B2A4.demoday.global.openai.dto.response.WhisperResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class STTService {

    private final WebClient webClient;

    public String convertToText(MultipartFile voiceFile) {
        if (voiceFile == null || voiceFile.isEmpty()) {
            throw new IllegalArgumentException("음성 파일이 비어있습니다.");
        }

        String originalFilename = voiceFile.getOriginalFilename();
        long fileSize = voiceFile.getSize();


        log.info("[STT 변환 시작] 파일명={}, 크기={}bytes", originalFilename, fileSize);

        try {
            String raw = transcribe(voiceFile);
            String polished = polish(raw);


            log.info("[STT 변환 완료] 결과={}", raw);
            log.info("[AI 다듬기 완료] 결과={}", polished);
            return polished;
            
        } catch (Exception e) {
            log.error("[STT 변환 실패] 파일명={}, 에러={}", originalFilename, e.getMessage());
            throw new RuntimeException("음성 파일 변환에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private String polish(String raw) {
        ChatRequest.Message system = new ChatRequest.Message(
                "system",
                "너는 한국어 문장을 자연스럽게 다듬는 편집자야. " +
                        "의미는 바꾸지 말고 말버릇, 반복, 말 끊김만 정리해서 깔끔한 문장으로 만들어줘."
        );

        ChatRequest.Message user = new ChatRequest.Message(
                "user",
                "다음은 음성 인식 결과야. 자연스럽게 정리해줘:\n\n" + raw
        );

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

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new IllegalStateException("GPT 응답이 비어 있습니다.");
        }

        return response.getChoices().get(0).getMessage().getContent();

    }

    private String transcribe(MultipartFile voiceFile) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", voiceFile.getResource());
        builder.part("model", "gpt-4o-mini-transcribe");

        try {

            WhisperResponse response = webClient.post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(WhisperResponse.class)
                    .block();

            if (response == null || response.getText() == null) {
                throw new IllegalStateException("Whisper 응답이 비어 있습니다.");
            }

            return response.getText();

        } catch (WebClientResponseException e) {
            String body = e.getResponseBodyAsString();
            log.error("OpenAI audio error. status={}, body={}", e.getStatusCode(), body, e);

            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                // 429라면 OpenAI 쿼터 문제
                throw new RuntimeException("현재 음성 인식 서비스 사용 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
            }

            throw e;
        }

    }
}
