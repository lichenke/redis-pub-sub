package heart.your.to.key.message;

import com.its.ione.v3.micro.message.stream.StreamCommands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.RecordId;
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

    @Autowired
    private StreamCommands commands;


    public void publish(String topic, Object message) {
        log.info("Publishing message...");
        redisTemplate.convertAndSend(topic, message);
    }

    public void publish() {
        log.info("Publishing stream message...");
        for (int i = 0; i < 10; i++) {
            RecordId id = commands.xAdd("s1", Collections.singletonMap("index", i));
            log.info("send message: {}", id.toString() + "-->" + i);
        }
    }

    @Data
    @AllArgsConstructor
    static class Person {
        private String name;
        private int age;
    }
}
