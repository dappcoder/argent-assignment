package malex.argent.watcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class TransferWatcherApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplicationBuilder(TransferWatcherApplication.class)
                .build();

        application.run(args);
    }
}
