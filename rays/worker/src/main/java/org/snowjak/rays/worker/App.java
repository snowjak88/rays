package org.snowjak.rays.worker;

import static org.apache.commons.math3.util.FastMath.max;

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
	private int parallelism;
	
	@Value("${rays.worker.queueSize}")
	private int queueSize;
	
	public static void main(String[] args) {
		
		SpringApplication.run(App.class, args);
	}
	
	private int getParallelism() {
		if (parallelism < 1)
			parallelism = max(1, Runtime.getRuntime().availableProcessors() - 1);
		return parallelism;
	}
	
	private int getQueueSize() {
		if (queueSize < 1)
			queueSize = 1;
		return queueSize;
	}
	
	@Bean("renderTaskExecutor")
	public ListeningExecutorService renderTaskExecutor() {
		
		final var executor = MoreExecutors
				.listeningDecorator(new ThreadPoolExecutor(getParallelism(), getParallelism(), 30, TimeUnit.SECONDS,
						new LinkedBlockingQueue<>(getQueueSize()), new BlocksUntilReadyRejectedExecutionHandler()));
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> executor.shutdownNow()));
		return executor;
	}
	
	@Bean("renderResultExecutor")
	public ListeningExecutorService renderResultExecutor() {
		
		final var executor = MoreExecutors
				.listeningDecorator(new ThreadPoolExecutor(getParallelism(), getParallelism(), 30, TimeUnit.SECONDS,
						new LinkedBlockingQueue<>(getQueueSize()), new BlocksUntilReadyRejectedExecutionHandler()));
		
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
