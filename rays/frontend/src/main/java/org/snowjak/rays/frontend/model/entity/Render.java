package org.snowjak.rays.frontend.model.entity;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
	
	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	private Render parent = null;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "parent")
	private Collection<Render> children = new LinkedList<>();
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private Scene scene;
	
	@Basic(optional = false)
	private String samplerJson = "";
	
	@Basic(optional = false)
	private String rendererJson = "";
	
	@Basic(optional = false)
	private String filmJson = "";
	
	@Basic
	private int percentComplete = 0;
	
	@Lob
	@Basic(optional = true, fetch = FetchType.LAZY)
	@Column(length = 4194304)
	private String pngBase64 = null;
	
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
	
	public boolean isChild() {
		
		return (getParent() != null);
	}
	
	public Render getParent() {
		
		return parent;
	}
	
	public boolean isParent() {
		
		return (getChildren() != null && !getChildren().isEmpty());
	}
	
	public Collection<Render> getChildren() {
		
		return children;
	}
	
	public void setChildren(Collection<Render> children) {
		
		this.children = children;
	}
	
	public void setParent(Render parent) {
		
		this.parent = parent;
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
	
	public String getPngBase64() {
		
		return pngBase64;
	}
	
	public void setPngBase64(String pngBase64) {
		
		this.pngBase64 = pngBase64;
	}
	
}
