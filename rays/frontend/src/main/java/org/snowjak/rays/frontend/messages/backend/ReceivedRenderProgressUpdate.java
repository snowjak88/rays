package org.snowjak.rays.frontend.messages.backend;

import org.snowjak.rays.RenderTask.ProgressInfo;

public class ReceivedRenderProgressUpdate {
	
	private final ProgressInfo info;
	
	public ReceivedRenderProgressUpdate(ProgressInfo info) {
		
		this.info = info;
	}
	
	public ProgressInfo getInfo() {
		
		return info;
	}
	
}
