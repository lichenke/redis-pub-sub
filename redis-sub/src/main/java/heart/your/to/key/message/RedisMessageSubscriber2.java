package heart.your.to.key.message;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author LiChenke
 **/

@Slf4j
@Component
public class RedisMessageSubscriber2 {

    public void receiveMsg(Object msg) {
        log.info("Received message...");
        log.info(JSONObject.toJSONString(msg));
    }
}
