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

import org.snowjak.rays.RenderTask;
import org.snowjak.rays.Settings;
import org.snowjak.rays.film.Film;
import org.snowjak.rays.renderer.Renderer;
import org.snowjak.rays.sampler.Sampler;
import org.springframework.data.annotation.CreatedDate;

import com.google.gson.JsonParseException;

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
	
	public Sampler getSampler() throws JsonParseException {
		
		return Settings.getInstance().getGson().fromJson(getSamplerJson(), Sampler.class);
	}
	
	public void setSampler(Sampler sampler) {
		
		setSamplerJson(Settings.getInstance().getGson().toJson(sampler));
	}
	
	public String getSamplerJson() {
		
		return samplerJson;
	}
	
	public void setSamplerJson(String samplerJson) {
		
		this.samplerJson = samplerJson;
	}
	
	public Renderer getRenderer() throws JsonParseException {
		
		return Settings.getInstance().getGson().fromJson(getRendererJson(), Renderer.class);
	}
	
	public void setRenderer(Renderer renderer) {
		
		setRendererJson(Settings.getInstance().getGson().toJson(renderer));
	}
	
	public String getRendererJson() {
		
		return rendererJson;
	}
	
	public void setRendererJson(String rendererJson) {
		
		this.rendererJson = rendererJson;
	}
	
	public Film getFilm() throws JsonParseException {
		
		return Settings.getInstance().getGson().fromJson(getFilmJson(), Film.class);
	}
	
	public void setFilm(Film film) {
		
		setFilmJson(Settings.getInstance().getGson().toJson(film));
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
	
	public RenderTask getRenderTask() throws JsonParseException {
		
		return new RenderTask(UUID.fromString(getUuid()), getSampler(), getRenderer(), getFilm(),
				getScene().getScene());
	}
	
}
