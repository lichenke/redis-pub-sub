package heart.your.to.key.controller;

import heart.your.to.key.message.RedisMessagePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author LiChenke
 **/

@RestController
@RequestMapping(value = "/redisMsg")
public class PublishController {

    @Autowired
    private RedisMessagePublisher publisher;

    @GetMapping("/publish/{topic}/{message}")
    public String publishTopicMsg(@PathVariable("topic") String topic,
                                @PathVariable("message") String message) {
        publisher.publish(topic, message);
        return "Publishing message...";
    }

    @GetMapping("/publishStream")
    public String publishStreamMsg() {
        publisher.publishStream();
        return "succeed!";
    }

    @GetMapping("/ack")
    public String ack() {
        publisher.ackMsg();
        return "succeed!";
    }
}
