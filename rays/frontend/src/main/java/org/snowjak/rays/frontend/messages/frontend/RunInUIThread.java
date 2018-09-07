package org.snowjak.rays.frontend.messages.frontend;

public class RunInUIThread {
	
	private final Runnable task;
	
	public RunInUIThread(Runnable task) {
		
		this.task = task;
	}
	
	public Runnable getTask() {
		
		return task;
	}
	
}
