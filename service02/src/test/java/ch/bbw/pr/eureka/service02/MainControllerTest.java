package ch.bbw.pr.eureka.service02;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MainControllerTest {
    @Test
    void booksEndpointExists() throws Exception {
        BooksClient booksClient = mock(BooksClient.class);
        when(booksClient.getBooks()).thenReturn(List.of(
                new Book(1, "The Metamorphosis", "Franz Kafka"),
                new Book(2, "The Trial", "Franz Kafka"),
                new Book(3, "1984", "George Orwell"),
                new Book(4, "Animal Farm", "George Orwell")));
        var mvc = MockMvcBuilders.standaloneSetup(new MainController(mock(RestTemplate.class), booksClient)).build();
        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("The Metamorphosis (Franz Kafka)"))
                .andExpect(jsonPath("$[1]").value("The Trial (Franz Kafka)"))
                .andExpect(jsonPath("$[2]").value("1984 (George Orwell)"))
                .andExpect(jsonPath("$[3]").value("Animal Farm (George Orwell)"))
                .andExpect(jsonPath("$.length()").value(4));
        verify(booksClient).getBooks();
    }

    @Test
    void addBookEndpointForwardsRequest() throws Exception {
        BooksClient booksClient = mock(BooksClient.class);
        Book book = new Book(5, "Harry Potter and the Philosopher's Stone", "J.K. Rowling");
        when(booksClient.addBook(book)).thenReturn(book);
        var mvc = MockMvcBuilders.standaloneSetup(new MainController(mock(RestTemplate.class), booksClient)).build();
        mvc.perform(post("/books").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":5,\"title\":\"Harry Potter and the Philosopher's Stone\",\"author\":\"J.K. Rowling\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Harry Potter and the Philosopher's Stone"));
        verify(booksClient).addBook(book);
    }

    @Test
    void callUsesEurekaServiceNameAndEnrichesResponse() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity("http://service-01/api", MyData.class))
                .thenReturn(ResponseEntity.ok(new MyData("Hello World from Service 01")));

        MyResponse response = new MainController(restTemplate, mock(BooksClient.class)).callService01();

        verify(restTemplate).getForEntity("http://service-01/api", MyData.class);
        assertEquals("Hello World from Service 01", response.originalData().name());
        assertEquals("Antwort von service-01 erfolgreich verarbeitet", response.message());
        assertNotNull(response.timestamp());
    }
}
