package heart.your.to.key.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author LiChenke
 **/
@Slf4j
@Component
public class RedisMessagePublisher {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void publish(String topic, Object message) {
        log.info("Publishing message...");
        redisTemplate.convertAndSend(topic, message);
    }
}
