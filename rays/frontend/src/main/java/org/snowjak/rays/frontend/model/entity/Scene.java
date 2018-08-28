package org.snowjak.rays.frontend.model.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;

@Entity
public class Scene {
	
	@Id
	private long id;
	
	@Version
	private long version;
	
	@Lob
	@Basic(fetch = FetchType.EAGER, optional = false)
	@Column(length = 65536)
	private String json = "";
	
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
	
	public String getJson() {
		
		return json;
	}
	
	public void setJson(String json) {
		
		this.json = json;
	}
	
}
