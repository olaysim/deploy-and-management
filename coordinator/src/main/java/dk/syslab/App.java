package dk.syslab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class App {
//    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = SpringApplication.run(App.class, args);
    }
}
