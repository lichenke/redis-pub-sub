package heart.your.to.key.controller;

import heart.your.to.key.pojo.Consumer;
import heart.your.to.key.service.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author LiChenke
 **/
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Autowired
    private ConsumerService service;

    @PostMapping("/register")
    public void register(@RequestBody Consumer consumer) {
        service.register(consumer);
    }
}
