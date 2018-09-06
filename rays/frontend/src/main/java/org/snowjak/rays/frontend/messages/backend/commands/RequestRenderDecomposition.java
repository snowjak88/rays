package org.snowjak.rays.frontend.messages.backend.commands;

import java.util.Collection;
import java.util.UUID;

public class RequestRenderDecomposition extends AbstractChainableCommand<UUID, Collection<UUID>> {
	
	private final int regionSize;
	
	public RequestRenderDecomposition(int regionSize) {
		
		this(null, regionSize);
	}
	
	public RequestRenderDecomposition(UUID uuid, int regionSize) {
		
		super(uuid);
		this.regionSize = regionSize;
	}
	
	public UUID getUuid() {
		
		return getContext();
	}
	
	public int getRegionSize() {
		
		return regionSize;
	}
	
}
