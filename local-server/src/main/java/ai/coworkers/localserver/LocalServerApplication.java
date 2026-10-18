package ai.coworkers.localserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ai.coworkers")
public class LocalServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalServerApplication.class, args);
    }
}
