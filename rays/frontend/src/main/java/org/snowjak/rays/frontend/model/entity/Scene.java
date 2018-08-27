package org.snowjak.rays.frontend.model.entity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;

import org.snowjak.rays.Settings;

import com.google.gson.JsonParseException;

@Entity
public class Scene {
	
	@Id
	private long id;
	
	@Version
	private long version;
	
	@Lob
	@Basic(fetch = FetchType.EAGER, optional = false)
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
	
	public org.snowjak.rays.Scene getScene() throws JsonParseException {
		
		return Settings.getInstance().getGson().fromJson(getJson(), org.snowjak.rays.Scene.class);
	}
	
	public void setScene(org.snowjak.rays.Scene scene) {
		
		setJson(Settings.getInstance().getGson().toJson(scene));
	}
	
}
