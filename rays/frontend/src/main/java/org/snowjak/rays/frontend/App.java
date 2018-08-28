package org.snowjak.rays.frontend;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "org.snowjak.rays.frontend")
@EnableRabbit
@EnableTransactionManagement
public class App {
	
	@Value("${rabbitmq.taskq}")
	private String renderTaskQueueName;
	
	@Value("${rabbitmq.progressq}")
	private String renderProgressQueueName;
	
	@Value("${rabbitmq.resultq}")
	private String renderResultQueueName;
	
	public static void main(String[] args) {
		
		SpringApplication.run(App.class, args);
	}
	
	@Autowired
	@Bean
	public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
		
		final var admin = new RabbitAdmin(connectionFactory);
		admin.declareQueue(taskQueue());
		admin.declareQueue(progressQueue());
		admin.declareQueue(resultQueue());
		return admin;
	}
	
	@Bean
	public Queue taskQueue() {
		
		return new Queue(renderTaskQueueName);
	}
	
	@Bean
	public Queue progressQueue() {
		
		return new Queue(renderProgressQueueName);
	}
	
	@Bean
	public Queue resultQueue() {
		
		return new Queue(renderResultQueueName);
	}
}
