package ch.bbw.pr.eureka.service01;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.DirtiesContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
@AutoConfigureMockMvc
class MainControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void booksReturnsCourseCatalog() throws Exception {
        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("The Metamorphosis"))
                .andExpect(jsonPath("$[0].author").value("Franz Kafka"))
                .andExpect(jsonPath("$[1].title").value("The Trial"))
                .andExpect(jsonPath("$[1].author").value("Franz Kafka"))
                .andExpect(jsonPath("$[2].title").value("1984"))
                .andExpect(jsonPath("$[2].author").value("George Orwell"))
                .andExpect(jsonPath("$[3].title").value("Animal Farm"))
                .andExpect(jsonPath("$[3].author").value("George Orwell"))
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void addBookAppearsInCatalog() throws Exception {
        String body = "{\"id\":5,\"title\":\"Harry Potter and the Philosopher's Stone\",\"author\":\"J.K. Rowling\"}";
        mvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[4].title").value("Harry Potter and the Philosopher's Stone"))
                .andExpect(jsonPath("$[4].author").value("J.K. Rowling"));
    }

    @Test
    void apiReturnsServiceName() throws Exception {
        mvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hello World from Service 01"));
    }
}
