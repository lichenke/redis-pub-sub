package heart.your.to.key.message;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RStream;
import org.redisson.api.StreamMessageId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.BoundStreamOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;

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

    public void publishStream() {
        log.info("Publishing stream message...");
        BoundStreamOperations<String, String, String>
                operations = redisTemplate.boundStreamOps("s1");
        for (int i = 0; i < 10; i++) {
            RecordId recordId = operations.add(Collections.singletonMap("index", String.valueOf(i)));
            System.out.printf("send message %d , message id %s\n", i, recordId);
        }
    }
}
