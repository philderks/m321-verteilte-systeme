package ch.bbw.pr.eureka.service01;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class RequestLoggingTest {
    @Autowired MockMvc mvc;

    @Test
    void logsCompletedRequestMetadataWithoutPayloadOrQuery(CapturedOutput output) throws Exception {
        String secret = "private-title-should-not-be-logged";
        mvc.perform(post("/books?token=private-query-should-not-be-logged")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":99,\"title\":\"" + secret + "\",\"author\":\"Anonymous\"}"))
                .andExpect(status().isCreated());

        assertThat(output.getOut()).containsPattern("POST /books status=201 durationMs=\\d+");
        assertThat(output.getOut()).doesNotContain(secret, "private-query-should-not-be-logged");
    }
}
