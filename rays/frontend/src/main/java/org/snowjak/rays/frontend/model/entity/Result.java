package org.snowjak.rays.frontend.model.entity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class Result {
	
	@Id
	private long id;
	
	@Version
	private long version;
	
	@OneToOne(fetch = FetchType.EAGER, mappedBy = "result", optional = false)
	private Render render;
	
	@Lob
	@Basic(optional = false)
	private String pngBase64 = "";
	
	public long getId() {
		
		return id;
	}
	
	public void setId(long id) {
		
		this.id = id;
	}
	
	public long getVersion() {
		
		return version;
	}
	
	public void setVersion(long version) {
		
		this.version = version;
	}
	
	public Render getRender() {
		
		return render;
	}
	
	public void setRender(Render render) {
		
		this.render = render;
	}
	
	public String getPngBase64() {
		
		return pngBase64;
	}
	
	public void setPngBase64(String pngBase64) {
		
		this.pngBase64 = pngBase64;
	}
}