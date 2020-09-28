package heart.your.to.key.config;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author LiChenke
 **/

@Slf4j
@Configuration
public class MessageListenerContainerConfig {

    @Bean
    @DependsOn({"topic1", "topic2"})
    public RedisMessageListenerContainer redisMessageListenerContainer(
            Map<MessageListener, Set<Topic>> messageListeners,
            RedisConnectionFactory redisConnfactory) {
        if (messageListeners.size() == 0) {
            return null;
        }
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnfactory);
        container.setMessageListeners(Collections.unmodifiableMap(messageListeners));
        List<String> tNames = Lists.newArrayList();
        messageListeners.forEach((key, value) -> tNames
                .addAll(value.stream().map(Topic::getTopic).collect(Collectors.toList())));
        log.info("Redis消息订阅池配置完毕，已成功订阅{}个主题: {}", tNames.size(), tNames);
        return container;
    }
}
