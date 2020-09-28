package heart.your.to.key.message;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author LiChenke
 **/

@Slf4j
@Component
public class RedisMessageSubscriber implements MessageListener {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        Object obj = redisTemplate.getValueSerializer().deserialize(message.getBody());
        String channel = new String(message.getChannel());
        log.info("Received message...");
        log.info(channel + ": " + JSONObject.toJSONString(obj));
    }
}   
