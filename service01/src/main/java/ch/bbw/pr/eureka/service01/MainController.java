package ch.bbw.pr.eureka.service01;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @GetMapping("/api")
    public MyData getData() {
        return new MyData("Hello World from Service 01");
    }
}
