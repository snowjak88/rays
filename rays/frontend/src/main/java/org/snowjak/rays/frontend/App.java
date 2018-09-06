package org.snowjak.rays.frontend;

import static org.apache.commons.math3.util.FastMath.min;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

@SpringBootApplication(scanBasePackages = "org.snowjak.rays.frontend")
@EnableRabbit
@EnableTransactionManagement
@EnableJpaAuditing
public class App {
	
	@Value("${rabbitmq.taskq}")
	private String renderTaskQueueName;
	
	@Value("${rabbitmq.progressq}")
	private String renderProgressQueueName;
	
	@Value("${rabbitmq.resultq}")
	private String renderResultQueueName;
	
	@Value("${org.snowjak.rays.backend.parallelism}")
	private int backendParallelism = 2;
	
	@Value("${org.snowjak.rays.frontend.parallelism}")
	private int frontendParallelism = 1;
	
	public static void main(String[] args) {
		
		SpringApplication.run(App.class, args);
	}
	
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
	
	@Bean
	public EventBus backendEventBus() {
		
		return new AsyncEventBus("backend", backendMessageExecutor());
	}
	
	@Bean
	public ExecutorService backendMessageExecutor() {
		
		return Executors.newFixedThreadPool(frontendParallelism);
	}
	
	@Bean
	@Lazy
	@Scope(scopeName = "vaadin-session", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public EventBus frontendEventBus() {
		
		return new AsyncEventBus("frontend", frontendMessageExecutor());
	}
	
	@Bean
	@Lazy
	@Scope(scopeName = "vaadin-session", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public ExecutorService frontendMessageExecutor() {
		
		return Executors.newFixedThreadPool(frontendParallelism);
	}
	
	@Bean
	public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory() {
		
		final var rabbit = new SimpleRabbitListenerContainerFactory();
		rabbit.setConcurrentConsumers(min(frontendParallelism, 2));
		rabbit.setMaxConcurrentConsumers(frontendParallelism);
		rabbit.setReceiveTimeout(15000l);
		return rabbit;
	}
	
}
