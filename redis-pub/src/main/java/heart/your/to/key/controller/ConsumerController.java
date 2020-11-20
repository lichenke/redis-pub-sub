package heart.your.to.key.controller;

import com.its.ione.v3.cmdb.api.pojo.Ci;
import com.its.ione.v3.cmdb.service.micro.CiRegisterTask.RegisterConfig;
import com.its.ione.v3.core.jsonrpc.tools.JwtRpcClient;
import com.its.ione.v3.micro.register.consumer.ConsumerUtils;
import com.its.ione.v3.micro.register.consumer.pojo.Consumer;
import com.its.ione.v3.micro.register.consumer.pojo.JWTCredential;

import com.alibaba.fastjson.JSON;
import heart.your.to.key.util.Consumers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.Supplier;

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

    @PostMapping("/consumer/register")
    public String registerConsumer(@RequestBody Consumer consumer) {
        ConsumerUtils.registerConsumer(consumer);
        return consumer.getUsername() + "注册成功!";
    }

    @PostMapping("/consumer/deployJWT")
    public String deployJWT(String usernameOrId) {
        ConsumerUtils.deployJWTCredential(usernameOrId);
        return "成功";
    }

    @PostMapping("/consumer/deployJWT2")
    public String deployJWT(String usernameOrId, String key, String secret) {
        ConsumerUtils.deployJWTCredential(usernameOrId, key, secret);
        return "成功";
    }

    @GetMapping("/consumer/test")
    public String test() {
        Supplier<String> supplier = ConsumerUtils.buildJwtSupplier("user");
        JwtRpcClient client = new JwtRpcClient(supplier, "http://localhost:8000/relax/rpc");
        Ci ci = client.safeCall(Ci.class, "/v2/login/login", "admin", "admin1", false);
        return JSON.toJSONString(ci);
    }

    @PostMapping("/home/register/cis")
    public String registerCis(@RequestBody RegisterConfig config) {
        StringBuilder sb = new StringBuilder("ci info:");
        List<Ci> cis = config.getCis();
        cis.forEach(ci -> sb.append(ci.getName()).append(" "));
        sb.append(config.getConfig().get("type")).append(" ").append(config.getConfig().get("application"));
        log.info(sb.toString());
        return sb.toString();
    }
}
