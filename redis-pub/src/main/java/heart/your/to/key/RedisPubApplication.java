package heart.your.to.key;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.its.ione","heart.your.to.key"})
public class RedisPubApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisPubApplication.class, args);
    }

}
