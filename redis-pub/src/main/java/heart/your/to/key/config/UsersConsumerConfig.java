package heart.your.to.key.config;

import static com.google.common.base.Preconditions.checkNotNull;

import com.its.ione.v3.micro.register.service.KongProperties;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * home服务启动时自动向KONG网关注册基础用户Consumer
 *
 * @author LiChenke
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(UsersConsumerConfig.UserConsumerProperties.class)
public class UsersConsumerConfig {

    @Autowired
    private UserConsumerProperties userConsumer;

    @Autowired
    private KongProperties kongProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Bean
    @ConditionalOnProperty(name = {"register.user.username", "gateway.kong.adminUrl"})
    public void registerUserConsumer() {
        String adminUrl = kongProperties.getAdminUrl();
        String username = userConsumer.username;
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(3);
        param.put("username", username);
        param.put("custom_id", userConsumer.customId);
        param.put("tags", userConsumer.tags);

        String cUrl = adminUrl + "/consumers";
        String qUrl = cUrl.concat("/" + username);

        boolean exists = true;
        try {
            restTemplate.getForObject(qUrl, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            exists = false;
        }

        try {
            if (exists) {
                restTemplate.patchForObject(qUrl, param, Map.class);
            } else {
                restTemplate.postForObject(cUrl, param, Map.class);
            }
            log.info("Users_Consumer注册成功!");
        } catch (Exception e) {
            log.info("Users_Consumer注册失败! 请检查配置", e);
            return;
        }
        deployJWTCredential(qUrl);
    }

    private void deployJWTCredential(String url) {
        String jUrl = url.concat("/jwt");
        Map<String, String> param = Maps.newHashMapWithExpectedSize(3);
        param.put("algorithm", "HS256");
        param.put("key", userConsumer.key);
        param.put("secret", userConsumer.secret);
        try {
            JSONObject jwtCredentials = restTemplate.getForObject(jUrl, JSONObject.class);
            for (Object o : checkNotNull(jwtCredentials).getJSONArray("data")) {
                JSONObject credential = (JSONObject) o;
                if (credential.getString("key").equals(userConsumer.key)) {
                    String id = credential.getString("id");
                    restTemplate.delete(jUrl.concat("/" + id));
                }
            }
            restTemplate.postForObject(jUrl, param, Map.class);
            log.info("Users_Consumer的jwt凭证部署成功!");
        } catch (Exception e) {
            log.info("Users_Consumer的jwt凭证部署失败! 请检查配置", e);
        }
    }

    @Data
    @ConfigurationProperties(prefix = "register.user")
    static class UserConsumerProperties {

        private String username;
        private String customId;
        private List<String> tags;
        private String key;
        private String secret;
    }
}
