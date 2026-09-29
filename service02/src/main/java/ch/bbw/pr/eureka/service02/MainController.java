package ch.bbw.pr.eureka.service02;

import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class MainController {
    private final RestTemplate restTemplate;

    public MainController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/call")
    public MyResponse callService01() {
        ResponseEntity<MyData> response = restTemplate.getForEntity("http://service-01/api", MyData.class);
        return new MyResponse(response.getBody(), "Antwort von service-01 erfolgreich verarbeitet", LocalDateTime.now().toString());
    }
}
