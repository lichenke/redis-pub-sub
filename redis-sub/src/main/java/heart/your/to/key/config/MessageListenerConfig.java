package heart.your.to.key.config;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import heart.your.to.key.message.RedisMessageSubscriber;
import heart.your.to.key.message.RedisMessageSubscriber2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author LiChenke
 **/
@Slf4j
@Configuration
public class MessageListenerConfig {

    @Bean
    public Map<MessageListener, Set<Topic>> messageListeners() {
        // 提供一个空map，可添加多个监听通道和对应的主题
        return Maps.newHashMap();
    }

    @Bean("listener1")
    public RedisMessageSubscriber listener1() {
        return new RedisMessageSubscriber();
    }

    @Bean("listener2")
    public MessageListenerAdapter listener2(RedisMessageSubscriber2 receiver) {
        return new MessageListenerAdapter(receiver, "receiveMsg");
    }

    @Bean("topic1")
    public ChannelTopic topic1(@Qualifier("listener1") MessageListener listener,
                               Map<MessageListener, Set<Topic>> messageListeners) {
        ChannelTopic topic = ChannelTopic.of("chatroom");
        messageListeners.put(listener, Collections.singleton(topic));
        return topic;
    }

    @Bean("topic2")
    public ChannelTopic topic2(@Qualifier("listener2") MessageListener listener,
                               Map<MessageListener, Set<Topic>> messageListeners) {
        ChannelTopic topic = ChannelTopic.of("test");
        messageListeners.computeIfAbsent(listener, key -> Sets.newHashSet()).add(topic);
        return topic;
    }
}
