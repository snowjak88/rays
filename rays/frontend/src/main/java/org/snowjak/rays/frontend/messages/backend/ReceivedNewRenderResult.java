package org.snowjak.rays.frontend.messages.backend;

import org.snowjak.rays.film.Film.Image;

public class ReceivedNewRenderResult {
	
	private final Image image;
	
	public ReceivedNewRenderResult(Image image) {
		
		this.image = image;
	}
	
	public Image getImage() {
		
		return image;
	}
	
}
