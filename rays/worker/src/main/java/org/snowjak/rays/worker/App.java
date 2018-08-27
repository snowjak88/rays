package org.snowjak.rays.worker;

import java.util.concurrent.Executors;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

@SpringBootApplication(scanBasePackages = "org.snowjak.rays.worker")
public class App extends SpringApplication {
	
	@Value("${rabbitmq.taskq}")
	private String renderTaskQueueName;
	
	@Value("${rabbitmq.resultq}")
	private String renderResultQueueName;
	
	@Value("${rays.worker.threads}")
	private int parallelism = 1;
	
	public static void main(String[] args) {
		
		SpringApplication.run(App.class, args);
	}
	
	@Bean
	public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
			MessageListenerAdapter messageListenerAdapter) {
		
		final var container = new SimpleMessageListenerContainer(connectionFactory);
		container.setQueues(renderTaskQueue());
		container.setMessageListener(messageListenerAdapter);
		return container;
	}
	
	@Bean
	public MessageListenerAdapter listenerAdapter(RenderTaskReceiver receiver) {
		
		return new MessageListenerAdapter(receiver, "receive");
	}
	
	@Bean
	public Queue renderTaskQueue() {
		
		return new Queue(renderTaskQueueName);
	}
	
	@Bean
	public ListeningExecutorService renderTaskExecutor() {
		
		final var executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(parallelism));
		Runtime.getRuntime().addShutdownHook(new Thread(() -> executor.shutdownNow()));
		return executor;
	}
	
}
