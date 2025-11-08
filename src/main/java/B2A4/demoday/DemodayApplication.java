package B2A4.demoday;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DemodayApplication {

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
		// ASW 관련
		System.setProperty("AWS_ACCESS_KEY_ID", dotenv.get("AWS_ACCESS_KEY_ID"));
		System.setProperty("AWS_SECRET_ACCESS_KEY", dotenv.get("AWS_SECRET_ACCESS_KEY"));
		System.setProperty("AWS_REGION", dotenv.get("AWS_REGION"));
		System.setProperty("AWS_S3_BUCKET", dotenv.get("AWS_S3_BUCKET"));
		// AWS SDK가 찾는 소문자 키도 추가
		System.setProperty("aws.accessKeyId", dotenv.get("AWS_ACCESS_KEY_ID"));
		System.setProperty("aws.secretAccessKey", dotenv.get("AWS_SECRET_ACCESS_KEY"));
		System.setProperty("aws.region", dotenv.get("AWS_REGION"));

		SpringApplication.run(DemodayApplication.class, args);
	}
}
