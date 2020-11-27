package heart.your.to.key.message;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamGroup;
import org.redisson.api.StreamMessageId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author LiChenke
 **/
@Slf4j
@Component
public class RedisMessagePublisher {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedissonClient client;

    public void publish(String topic, Object message) {
        log.info("Publishing message...");
        redisTemplate.convertAndSend(topic, message);
    }

    public void publishStream() {
        log.info("Publishing stream message...");
        RStream<String, Object> stream = client.getStream("s1");
        for (int i = 0; i < 10; i++) {
            StreamMessageId index = stream.add("index", "string" + i);
            log.info("send message {}, id: {}", i, index);
        }
    }

    public void ackMsg() {
        log.info("Consuming msgs....");
        RStream<String, Object> stream = client.getStream("s1");
        createStream(stream);
        boolean exists = false;
        for (StreamGroup group : stream.listGroups()) {
            if ("g1".equals(group.getName())) {
                exists = true;
            }
        }
        if (!exists) {
            stream.createGroup("g1", StreamMessageId.ALL);
        }
        Map<StreamMessageId, Map<String, Object>> msgs = stream.readGroup("g1", "c1");
        msgs.forEach((key, value) -> {
            log.info(key + ":" + value.toString());
            stream.ack("g1", key);
        });
    }

    private void createStream(RStream<String, Object> stream) {
        StreamMessageId id = stream.add("test", "test");
        stream.remove(id);
    }
}
