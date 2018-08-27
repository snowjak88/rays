package org.snowjak.rays.frontend;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.snowjak.rays.frontend")
@EnableRabbit
public class App {
	
	public static void main(String[] args) {
		
		SpringApplication.run(App.class, args);
	}
	
}
