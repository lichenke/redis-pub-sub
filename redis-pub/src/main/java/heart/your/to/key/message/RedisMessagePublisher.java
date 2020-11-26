package heart.your.to.key.message;

import lombok.extern.slf4j.Slf4j;
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
        BoundStreamOperations<String, Object, Object> operations =
                redisTemplate.boundStreamOps("stream_mq_key_for_redispubsub");
        for (int i = 0; i < 10; i++) {
            RecordId recordId = operations.add(Collections.singletonMap("index", String.valueOf(i)));
            log.info("send message {} , message id {} \n", i, recordId);
        }
    }
}
