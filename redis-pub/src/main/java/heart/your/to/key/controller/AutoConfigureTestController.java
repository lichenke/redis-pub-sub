package heart.your.to.key.controller;

import com.its.ione.core.base.Exceptions;
import com.its.ione.v3.micro.util.JWTUtils;
import com.its.ione.v3.micro.util.JWTUtils.Payload;

import heart.your.to.key.demo.autoconfigure.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author LiChenke
 **/

@RestController
@RequestMapping(value = "/wrap")
public class AutoConfigureTestController {

    @Autowired
    private ExampleService exampleService;

    @GetMapping("{word}")
    public String wrap(@PathVariable("word") String word) {
        Payload payload = decodeJWTHeader();
        return exampleService.wrap(word);
    }

    protected Payload decodeJWTHeader() {
        ServletRequestAttributes attr =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String auth = attr.getRequest().getHeader("Authorization");
        if (auth == null) {
            return new Payload();
        }
        if (auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            return JWTUtils.decodePayload(token);
        }
        throw Exceptions.illegalState("无法识别的认证类型，无法进行解码");
    }
}
