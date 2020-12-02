package heart.your.to.key.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;

/**
 * @author LiChenke
 **/
@Slf4j
@Configuration
public class ListenerConfig {

    @Bean
    public StreamListener<String, MapRecord<String, String, Object>> listener() {
        return message -> {
            log.info("stream: {}, id: {}, value: {}",
                     message.getStream(),
                     message.getId().toString(),
                     message.getValue().toString());
        };
    }

}
