package ch.bbw.pr.eureka.service02;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import feign.RetryableException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "eureka.client.enabled=false", "spring.cloud.discovery.enabled=false" })
class BooksClientTimeoutTest {
    private static final AtomicBoolean SLOW_RESPONSE = new AtomicBoolean();
    private static final CountDownLatch SLOW_RESPONSE_FINISHED = new CountDownLatch(1);
    private static final HttpServer SERVER = startServer();

    @Autowired
    private BooksClient booksClient;

    @DynamicPropertySource
    static void localService01(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.openfeign.client.config.service-01.url",
                () -> "http://localhost:" + SERVER.getAddress().getPort());
    }

    @AfterAll
    static void stopServer() {
        SERVER.stop(0);
    }

    @Test
    void slowService01ResponseTimesOut() throws InterruptedException {
        SLOW_RESPONSE.set(true);
        try {
            RetryableException timeout = assertThrows(RetryableException.class, booksClient::getBooks);
            assertTrue(timeout.getCause() instanceof SocketTimeoutException,
                    () -> "Expected a socket read timeout, got " + timeout);
        } finally {
            boolean finished = SLOW_RESPONSE_FINISHED.await(3, TimeUnit.SECONDS);
            SLOW_RESPONSE.set(false);
            assertTrue(finished, "slow stub did not finish");
        }
    }

    @Test
    void ordinaryFeignGetAndPostRemainFunctional() {
        assertEquals(List.of(new Book(1, "The Trial", "Franz Kafka")), booksClient.getBooks());
        Book book = new Book(2, "1984", "George Orwell");
        assertEquals(book, booksClient.addBook(book));
    }

    private static HttpServer startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/books", BooksClientTimeoutTest::respond);
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot start local service-01 stub", e);
        }
    }

    private static void respond(HttpExchange exchange) throws IOException {
        boolean slow = SLOW_RESPONSE.get();
        try (exchange) {
            if (slow) {
                // Deliberately longer than the configured Feign read timeout; no external service needed.
                try {
                    Thread.sleep(Duration.ofSeconds(1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            String json = exchange.getRequestMethod().equals("POST")
                    ? new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)
                    : "[{\"id\":1,\"title\":\"The Trial\",\"author\":\"Franz Kafka\"}]";
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
        } catch (IOException ignored) {
            // The timeout test closes the client socket before the stub can write its response.
        } finally {
            if (slow) {
                SLOW_RESPONSE_FINISHED.countDown();
            }
        }
    }
}
