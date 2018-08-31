package org.snowjak.rays.frontend.messages;

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
