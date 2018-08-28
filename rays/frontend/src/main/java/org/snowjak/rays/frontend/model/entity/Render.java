package org.snowjak.rays.frontend.model.entity;

import java.time.Instant;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;

@Entity
public class Render {
	
	@Id
	private String uuid = UUID.randomUUID().toString();
	
	@Version
	private long version;
	
	@CreatedDate
	private Instant created;
	
	@Basic
	private Instant completed;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private Scene scene;
	
	@Basic(optional = false)
	private String samplerJson = "";
	
	@Basic(optional = false)
	private String rendererJson = "";
	
	@Basic(optional = false)
	private String filmJson = "";
	
	@Transient
	private int percentComplete = 0;
	
	@OneToOne(optional = true, orphanRemoval = true, fetch = FetchType.LAZY)
	private Result result = null;
	
	public String getUuid() {
		
		return uuid;
	}
	
	public void setUuid(String uuid) {
		
		this.uuid = uuid;
	}
	
	public long getVersion() {
		
		return version;
	}
	
	public void setVersion(long version) {
		
		this.version = version;
	}
	
	public Instant getCreated() {
		
		return created;
	}
	
	public void setCreated(Instant created) {
		
		this.created = created;
	}
	
	public Instant getCompleted() {
		
		return completed;
	}
	
	public void setCompleted(Instant completed) {
		
		this.completed = completed;
	}
	
	public Scene getScene() {
		
		return scene;
	}
	
	public void setScene(Scene scene) {
		
		this.scene = scene;
	}
	
	public String getSamplerJson() {
		
		return samplerJson;
	}
	
	public void setSamplerJson(String samplerJson) {
		
		this.samplerJson = samplerJson;
	}
	
	public String getRendererJson() {
		
		return rendererJson;
	}
	
	public void setRendererJson(String rendererJson) {
		
		this.rendererJson = rendererJson;
	}
	
	public String getFilmJson() {
		
		return filmJson;
	}
	
	public void setFilmJson(String filmJson) {
		
		this.filmJson = filmJson;
	}
	
	public int getPercentComplete() {
		
		return percentComplete;
	}
	
	public void setPercentComplete(int percentComplete) {
		
		this.percentComplete = percentComplete;
	}
	
	public Result getResult() {
		
		return result;
	}
	
	public void setResult(Result result) {
		
		this.result = result;
	}
	
}
