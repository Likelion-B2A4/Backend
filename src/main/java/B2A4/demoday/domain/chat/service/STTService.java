package B2A4.demoday.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * STT 서비스 구현체
 * TODO: 실제 STT API (Google Cloud Speech-to-Text, OpenAI Whisper 등) 연동 필요
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class STTService {

    public String convertToText(MultipartFile voiceFile) {
        if (voiceFile == null || voiceFile.isEmpty()) {
            throw new IllegalArgumentException("음성 파일이 비어있습니다.");
        }

        String originalFilename = voiceFile.getOriginalFilename();
        long fileSize = voiceFile.getSize();
        
        log.info("[STT 변환 시작] 파일명={}, 크기={}bytes", originalFilename, fileSize);

        try {
            // TODO: 실제 STT API 호출 로직 구현
            // 1. Google Cloud Speech-to-Text API
            // 2. OpenAI Whisper API
            // 3. AWS Transcribe
            // 4. 네이버 Clova Speech
            
            // 임시로 더미 텍스트 반환 (개발 단계)
            String dummyText = "[STT 변환된 텍스트] " + originalFilename + " 파일이 처리되었습니다.";
            
            log.info("[STT 변환 완료] 결과={}", dummyText);
            return dummyText;
            
        } catch (Exception e) {
            log.error("[STT 변환 실패] 파일명={}, 에러={}", originalFilename, e.getMessage());
            throw new RuntimeException("음성 파일 변환에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
