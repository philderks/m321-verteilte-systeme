package ch.bbw.pr.eureka.service02;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MainControllerTest {
    @Test
    void callUsesEurekaServiceNameAndEnrichesResponse() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity("http://service-01/api", MyData.class))
                .thenReturn(ResponseEntity.ok(new MyData("Hello World from Service 01")));

        MyResponse response = new MainController(restTemplate).callService01();

        verify(restTemplate).getForEntity("http://service-01/api", MyData.class);
        assertEquals("Hello World from Service 01", response.originalData().name());
        assertEquals("Antwort von service-01 erfolgreich verarbeitet", response.message());
        assertNotNull(response.timestamp());
    }
}
