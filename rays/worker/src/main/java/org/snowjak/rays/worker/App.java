package org.snowjak.rays.worker;

import static org.apache.commons.math3.util.FastMath.max;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

@SpringBootApplication(scanBasePackages = "org.snowjak.rays.worker")
public class App extends SpringApplication {
	
	private static final Logger LOG = LoggerFactory.getLogger(App.class);
	
	public static void main(String[] args) {
		
		SpringApplication.run(App.class, args);
	}
	
	@Bean("renderTaskExecutor")
	public ListeningExecutorService renderTaskExecutor(@Value("${rays.worker.threads}") int parallelism,
			@Value("${rays.worker.queueSize}") int queueSize) {
		
		if (parallelism < 1)
			parallelism = Runtime.getRuntime().availableProcessors() - 1;
		
		queueSize = max(queueSize, 1);
		
		LOG.info("Spinning up a RenderTask executor: {} threads, with a pending-queue {} tasks deep.", parallelism,
				queueSize);
		
		final var executor = MoreExecutors
				.listeningDecorator(new ThreadPoolExecutor(1, parallelism, 30, TimeUnit.SECONDS,
						new LinkedBlockingQueue<>(queueSize), new BlocksUntilReadyRejectedExecutionHandler()));
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> executor.shutdownNow()));
		return executor;
	}
	
	@Bean("renderResultExecutor")
	public ListeningExecutorService renderResultExecutor() {
		
		final var executor = MoreExecutors.listeningDecorator(MoreExecutors.newDirectExecutorService());
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> executor.shutdownNow()));
		return executor;
	}
	
	public static class BlocksUntilReadyRejectedExecutionHandler implements RejectedExecutionHandler {
		
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			
			try {
				LOG.debug("Waiting to drop another task into the executor {} ...", executor.toString());
				executor.getQueue().put(r);
			} catch (InterruptedException e) {
				// do nothing here
			}
		}
		
	}
	
}
