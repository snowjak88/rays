package org.snowjak.rays.support.model.entity;

import java.time.Instant;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.snowjak.rays.Settings;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.film.Film;
import org.snowjak.rays.renderer.Renderer;
import org.snowjak.rays.sampler.Sampler;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.google.gson.JsonParseException;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "render_setup")
public class RenderSetup {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Version
	private long version;
	
	@CreatedDate
	private Instant created;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = { CascadeType.REFRESH })
	private Scene scene;
	
	@Basic(optional = false)
	@Column(name = "sampler_json")
	private String samplerJson = "";
	
	@Basic(optional = false)
	@Column(name = "renderer_json")
	private String rendererJson = "";
	
	@Basic(optional = false)
	@Column(name = "film_json")
	private String filmJson = "";
	
	@Basic(optional = false)
	@Column(name = "camera_json")
	private String cameraJson = "";
	
	private transient Sampler sampler = null;
	private transient Renderer renderer = null;
	private transient Film film = null;
	private transient Camera camera = null;
	
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
	
	public Instant getCreated() {
		
		return created;
	}
	
	public void setCreated(Instant created) {
		
		this.created = created;
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
		this.sampler = null;
	}
	
	public String getRendererJson() {
		
		return rendererJson;
	}
	
	public void setRendererJson(String rendererJson) {
		
		this.rendererJson = rendererJson;
		this.renderer = null;
	}
	
	public String getFilmJson() {
		
		return filmJson;
	}
	
	public void setFilmJson(String filmJson) {
		
		this.filmJson = filmJson;
		this.film = null;
	}
	
	public String getCameraJson() {
		
		return cameraJson;
	}
	
	public void setCameraJson(String cameraJson) {
		
		this.cameraJson = cameraJson;
		this.camera = null;
	}
	
	public Sampler inflateSampler() throws JsonParseException {
		
		if (sampler == null)
			sampler = Settings.getInstance().getGson().fromJson(getSamplerJson(), Sampler.class);
		return sampler;
	}
	
	public Renderer inflateRenderer() throws JsonParseException {
		
		if (renderer == null)
			renderer = Settings.getInstance().getGson().fromJson(getRendererJson(), Renderer.class);
		return renderer;
	}
	
	public Film inflateFilm() throws JsonParseException {
		
		if (film == null)
			film = Settings.getInstance().getGson().fromJson(getFilmJson(), Film.class);
		return film;
	}
	
	public Camera inflateCamera() throws JsonParseException {
		
		if (camera == null)
			camera = Settings.getInstance().getGson().fromJson(getCameraJson(), Camera.class);
		return camera;
	}
}
