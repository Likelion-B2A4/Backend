package B2A4.demoday;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DemodayApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemodayApplication.class, args);
	}


}
