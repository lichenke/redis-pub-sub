package heart.your.to.key.util;


import static com.google.common.base.Preconditions.checkNotNull;
import static heart.your.to.key.util.MicroServiceAttributes.CLIENT_ID;
import static heart.your.to.key.util.MicroServiceAttributes.CREDENTIAL_KEY;
import static heart.your.to.key.util.MicroServiceAttributes.CREDENTIAL_SECRET;


import com.its.ione.core.time.DateTimes;
import com.its.ione.v3.cmdb.api.pojo.Ci;
import com.its.ione.v3.micro.register.consumer.ConsumerUtils;
import com.its.ione.v3.micro.register.consumer.pojo.Consumer;
import com.its.ione.v3.micro.register.consumer.pojo.JWTCredential;
import com.its.ione.v3.micro.util.JWTUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消费者工具类
 *
 * @author LiLingbei
 */
public final class Consumers {

    public static Ci getConsumer(String appName) {
        checkNotNull(appName, "{appName} can not be null");
        Consumer consumer = ConsumerUtils.getConsumer(appName);
        Ci ci = new Ci();
        ci.setId(consumer.getId());
        ci.setName(consumer.getUsername());
        ci.put(CLIENT_ID, consumer.getCustom_id());
        List<JWTCredential> credentials = ConsumerUtils.getJWTCredentials(appName);
        if (credentials.size() > 0) {
            JWTCredential jwtCredential = credentials.get(0);
            ci.put(CREDENTIAL_KEY, jwtCredential.getKey());
            ci.put(CREDENTIAL_SECRET, jwtCredential.getSecret());
        }
        return ci;
    }

    public static String genJwtToken(Ci consumer) {
        JWTUtils.Payload payload = new JWTUtils.Payload();
        payload.put("appName", consumer.getName());
        payload.put("clientId", consumer.getString(CLIENT_ID));
        payload.setIss(consumer.getString(CREDENTIAL_KEY));
        LocalDateTime now = DateTimes.now();
        payload.setIat(DateTimes.toDate(now));
        payload.setExp(DateTimes.toDate(now.plusMinutes(30L)));
        return JWTUtils.createToken(payload, consumer.getString(CREDENTIAL_SECRET));
    }

    private Consumers() {
        throw new IllegalAccessError();
    }
}
