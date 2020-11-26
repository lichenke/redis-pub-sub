package heart.your.to.key.config;

import static java.lang.Boolean.TRUE;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author LiChenke
 **/

@Slf4j
@Configuration
public class StreamMessageListenerContainerConfig {

    private static String groupId;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory) {
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainerOptions.builder().batchSize(10).build();
        return StreamMessageListenerContainer.create(redisConnectionFactory, options);
    }

    @Bean
    public StreamListener<String, MapRecord<String, String, String>> streamListener(
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer,
            Environment environment) throws UnknownHostException {
        String consumer = Inet4Address.getLocalHost().getHostName() + ":" + environment.getProperty("server.port");
        log.info("streamKey = " + groupId + ", groupId = " + groupId + ", consumer = " + consumer);
        if (!TRUE.equals(redisTemplate.hasKey(groupId))) {
            RStream<String, Object> stream = redissonClient.getStream(groupId);
            stream.createGroup(groupId, StreamMessageId.ALL);
        }
        StreamListener<String, MapRecord<String, String, String>> listener = message -> {
            for (Map.Entry<String, String> entry : message) {
                log.info(entry.getKey() + entry.getValue());
            }
        };
        StreamOffset<String> streamOffset = StreamOffset.create(groupId, ReadOffset.lastConsumed());
        streamMessageListenerContainer.receiveAutoAck(Consumer.from(groupId, consumer), streamOffset, listener);
        return listener;
    }

    @Autowired
    private void setGroupId(Environment environment) {
        groupId = environment.getRequiredProperty("server.name");
    }

    @Component
    static class streamMessageListenerContainerListener implements ApplicationListener<ApplicationStartedEvent> {

        @Override
        public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
            ConfigurableApplicationContext context = applicationStartedEvent.getApplicationContext();
            context.getBeanProvider(StreamMessageListenerContainer.class)
                    .ifAvailable(Lifecycle::start);
        }
    }
}
