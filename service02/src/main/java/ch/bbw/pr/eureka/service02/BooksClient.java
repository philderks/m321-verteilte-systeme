package ch.bbw.pr.eureka.service02;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("service-01")
public interface BooksClient {
    @GetMapping("/books")
    List<Book> getBooks();

    @PostMapping("/books")
    Book addBook(@RequestBody Book book);
}
