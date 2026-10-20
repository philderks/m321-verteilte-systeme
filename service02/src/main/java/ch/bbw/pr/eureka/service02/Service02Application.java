package ch.bbw.pr.eureka.service02;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class Service02Application {

	public static void main(String[] args) {
		SpringApplication.run(Service02Application.class, args);
	}

}
