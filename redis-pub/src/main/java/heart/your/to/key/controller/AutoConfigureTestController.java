package heart.your.to.key.controller;

import heart.your.to.key.demo.autoconfigure.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return exampleService.wrap(word);
    }
}
