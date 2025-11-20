package B2A4.demoday.global.kakao.service;

import B2A4.demoday.global.kakao.dto.response.KakaoApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoAddressService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Value("${kakao.api.url}")
    private String kakaoApiUrl;

    public double[] getCoordinate(String address) {

        // WebClient 생성
        WebClient webClient = WebClient.builder()
                .baseUrl(kakaoApiUrl)
                .defaultHeader("Authorization", "KakaoAK " + kakaoApiKey) // 헤더 설정 중요
                .build();

        // API 호출
        KakaoApiResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("query", address)
                        .build())
                .retrieve()
                .bodyToMono(KakaoApiResponse.class)
                .block(); // 동기 처리 (필요 시 비동기로 변경 가능)

        // 결과 처리
        if (response == null || response.getDocuments().isEmpty()) {
            log.warn("주소를 찾을 수 없습니다: {}", address);
            return null;
        }

        // 첫 번째 검색 결과 가져오기
        KakaoApiResponse.Document document = response.getDocuments().get(0);

        double lng = Double.parseDouble(document.getX()); // 경도
        double lat = Double.parseDouble(document.getY()); // 위도

        return new double[]{lat, lng};
    }
}
