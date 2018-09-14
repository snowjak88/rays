package org.snowjak.rays.frontend.model.entity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Stream;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.snowjak.rays.Settings;
import org.snowjak.rays.film.Film;
import org.snowjak.rays.renderer.Renderer;
import org.snowjak.rays.sampler.Sampler;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.google.gson.JsonParseException;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "render")
public class Render {
	
	@Id
	@Column(length = 64)
	private String uuid = UUID.randomUUID().toString();
	
	@Version
	private long version;
	
	@CreatedDate
	private Instant created;
	
	@Basic
	private Instant submitted;
	
	@Basic
	private Instant completed;
	
	@Basic
	private boolean decomposed;
	
	@Basic
	private int width;
	
	@Basic
	private int height;
	
	@Basic
	private int spp;
	
	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	private Render parent = null;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "parent")
	private Collection<Render> children = new LinkedList<>();
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private Scene scene;
	
	@Basic(optional = false)
	@Column(name="sampler_json")
	private String samplerJson = "";
	
	@Basic(optional = false)
	@Column(name="renderer_json")
	private String rendererJson = "";
	
	@Basic(optional = false)
	@Column(name="film_json")
	private String filmJson = "";
	
	@Basic
	@Column(name="percent_complete")
	private int percentComplete = 0;
	
	@Lob
	@Basic(optional = true, fetch = FetchType.LAZY)
	@Column(name="png_base64", length = 4194304)
	private String pngBase64 = null;
	
	private transient Sampler sampler = null;
	private transient Renderer renderer = null;
	private transient Film film = null;
	private transient Resource result = null;
	
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
	
	public Instant getSubmitted() {
		
		return submitted;
	}
	
	public void setSubmitted(Instant submitted) {
		
		this.submitted = submitted;
	}
	
	public Instant getCompleted() {
		
		return completed;
	}
	
	public void setCompleted(Instant completed) {
		
		this.completed = completed;
	}
	
	public boolean isDecomposed() {
		
		return decomposed;
	}
	
	public void setDecomposed(boolean decomposed) {
		
		this.decomposed = decomposed;
	}
	
	public int getWidth() {
		
		return width;
	}
	
	public void setWidth(int width) {
		
		this.width = width;
	}
	
	public int getHeight() {
		
		return height;
	}
	
	public void setHeight(int height) {
		
		this.height = height;
	}
	
	public int getSpp() {
		
		return spp;
	}
	
	public void setSpp(int spp) {
		
		this.spp = spp;
	}
	
	public String getSize() {
		
		return getWidth() + "x" + getHeight() + "(" + getSpp() + "spp)";
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
	
	public Stream<Render> streamChildren() {
		
		return children.stream();
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
	
	public int getPercentComplete() {
		
		return percentComplete;
	}
	
	public Float getPercentCompleteFloat() {
		
		return ((float) percentComplete) / 100f;
	}
	
	public void setPercentComplete(int percentComplete) {
		
		this.percentComplete = percentComplete;
	}
	
	public String getPngBase64() {
		
		return pngBase64;
	}
	
	public void setPngBase64(String pngBase64) {
		
		this.pngBase64 = pngBase64;
		this.result = null;
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
	
	public Resource inflateResultAsResource() {
		
		if (result == null)
			result = new StreamResource(new StreamSource() {
				
				private static final long serialVersionUID = 5594542752451248017L;
				
				@Override
				public InputStream getStream() {
					
					if (getPngBase64() == null)
						return new ByteArrayInputStream(new byte[0]);
					
					return new ByteArrayInputStream(Base64.getDecoder().decode(getPngBase64()));
				}
			}, uuid + ".png");
		
		return result;
	}
	
}
