package org.snowjak.rays.worker;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

@SpringBootApplication(scanBasePackages = "org.snowjak.rays.worker")
public class App extends SpringApplication {
	
	@Value("${rays.worker.threads}")
	private int parallelism = 1;
	
	@Value("${rays.worker.queueSize}")
	private int queueSize = 4;
	
	public static void main(String[] args) {
		
		SpringApplication.run(App.class, args);
	}
	
	@Bean("renderTaskExecutor")
	public ListeningExecutorService renderTaskExecutor() {
		
		final var executor = MoreExecutors
				.listeningDecorator(new ThreadPoolExecutor(parallelism, parallelism, 30, TimeUnit.SECONDS,
						new LinkedBlockingQueue<>(queueSize), new BlocksUntilReadyRejectedExecutionHandler()));
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> executor.shutdownNow()));
		return executor;
	}
	
	@Bean("renderResultExecutor")
	public ListeningExecutorService renderResultExecutor() {
		
		final var executor = MoreExecutors
				.listeningDecorator(new ThreadPoolExecutor(parallelism, parallelism, 30, TimeUnit.SECONDS,
						new LinkedBlockingQueue<>(queueSize), new BlocksUntilReadyRejectedExecutionHandler()));
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> executor.shutdownNow()));
		return executor;
	}
	
	public static class BlocksUntilReadyRejectedExecutionHandler implements RejectedExecutionHandler {
		
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			
			try {
				executor.getQueue().put(r);
			} catch (InterruptedException e) {
				// do nothing here
			}
		}
		
	}
	
}
