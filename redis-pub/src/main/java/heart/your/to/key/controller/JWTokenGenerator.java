package heart.your.to.key.controller;

import com.its.ione.core.time.DateTimes;
import com.its.ione.v3.micro.util.JWTUtils;
import com.its.ione.v3.micro.util.JWTUtils.Payload;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * @author LiChenke
 **/

@RestController
@RequestMapping(value = "/genJWT")
public class JWTokenGenerator {

    @GetMapping("/{iss}/{secret}")
    public String genJWToken(@PathVariable String iss, @PathVariable String secret) {
        Payload payload = new Payload();
        payload.setIss(iss);
        LocalDateTime now = DateTimes.now();
        payload.setIat(DateTimes.toDate(now));
        payload.setExp(DateTimes.toDate(now.plusMinutes(30)));
        return JWTUtils.createToken(payload, secret);
    }
}
