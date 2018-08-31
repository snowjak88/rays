package org.snowjak.rays.frontend.messages;

import org.snowjak.rays.RenderTask.ProgressInfo;

public class RenderProgressUpdate {
	
	private final ProgressInfo info;
	
	public RenderProgressUpdate(ProgressInfo info) {
		
		this.info = info;
	}
	
	public ProgressInfo getInfo() {
		
		return info;
	}
	
}
