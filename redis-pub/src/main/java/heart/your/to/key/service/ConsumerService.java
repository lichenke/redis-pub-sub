package heart.your.to.key.service;

import com.its.ione.starter.register.KongTemplate;
import com.its.ione.starter.register.kong.req.ConsumerReq;
import com.its.ione.starter.register.kong.resp.ConsumerResp;

import heart.your.to.key.pojo.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author LiChenke
 **/

@Service
@Slf4j
public class ConsumerService {

    @Autowired
    private KongTemplate kongTemplate;

    public void register(Consumer consumer) {
        ConsumerReq req = new ConsumerReq.Builder().custom_id(consumer.getCustomId())
                                                   .username(consumer.getUsername())
                                                   .tags(consumer.getTags())
                                                   .build();
        try {
            ConsumerResp create = kongTemplate.createConsumer(req);
            if (create.isConflicted()) {
                ConsumerResp update = kongTemplate.updateConsumer(consumer.getUsername(), req);
                if (update.succeed()) {
                    log.info("Consumer {} already exists, successfully updated", consumer.getUsername());
                    return;
                }
            }
            log.info("Consumer {} successfully created", consumer.getUsername());
        } catch (Throwable e) {
            log.error(e.getMessage());
        }
    }

    public void register2(Consumer consumer) {
        ConsumerReq req = new ConsumerReq.Builder().custom_id(consumer.getCustomId())
                .username(consumer.getUsername())
                .build();
        try {
            ConsumerResp create = kongTemplate.updateOrCreateConsumer(consumer.getUsername(), req);
            log.info("Consumer {} successfully created", consumer.getUsername());
        } catch (Throwable e) {
            log.error(e.getMessage());
        }
    }
}
