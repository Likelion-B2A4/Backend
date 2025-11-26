package B2A4.demoday;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Date;
import java.util.TimeZone;

@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class DemodayApplication {

	@PostConstruct
	public void started() {
		// 서울 표준 시 고정
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	public static void main(String[] args) {
		// .env 파일 로드
		Dotenv dotenv = Dotenv.configure()
				.directory("./")   // 루트 경로에 .env 있을 때
				.ignoreIfMissing() // 없으면 무시
				.load();

		// Spring이 읽을 수 있게 시스템 프로퍼티로 등록
		// DB 관련
		System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

		// OPENAI 관련
		System.setProperty("OPENAI_API_KEY", dotenv.get("OPENAI_API_KEY"));

		// Kakao 관련
		System.setProperty("KAKAO_API_KEY", dotenv.get("KAKAO_API_KEY"));

		SpringApplication.run(DemodayApplication.class, args);
	}
}