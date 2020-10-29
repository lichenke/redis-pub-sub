package heart.your.to.key.controller;

import com.its.ione.v3.cmdb.api.pojo.Ci;
import com.its.ione.v3.micro.register.consumer.ConsumerUtils;
import com.its.ione.v3.micro.register.consumer.pojo.Consumer;
import com.its.ione.v3.micro.register.consumer.pojo.JWTCredential;

import com.alibaba.fastjson.JSON;
import heart.your.to.key.util.Consumers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author LiChenke
 **/

@RestController
@Slf4j
public class ConsumerController {

    @GetMapping("/consumer/{username}")
    public String getConsumer(@PathVariable("username") String username) {
        Consumer consumer = ConsumerUtils.getConsumer(username);
        return JSON.toJSONString(consumer);
    }

    @GetMapping("/jwt/{username}")
    public String getJWTCredentials(@PathVariable("username") String username) {
        List<JWTCredential> jwtCredentials = ConsumerUtils.getJWTCredentials(username);
        return JSON.toJSONString(jwtCredentials);
    }

    @GetMapping("/getJwtToken/{appName}")
    public String getJwtToken(@PathVariable String appName) {
        Ci consumer = Consumers.getConsumer(appName);
        return Consumers.genJwtToken(consumer);
    }
}
