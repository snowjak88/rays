package org.snowjak.rays.support.model.entity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Stream;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
	private int offsetX;
	
	@Basic
	private int offsetY;
	
	@Basic
	private int width;
	
	@Basic
	private int height;
	
	@Basic
	private int spp;
	
	@ManyToOne(fetch = FetchType.EAGER, optional = true, cascade = { CascadeType.REFRESH })
	private Render parent = null;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "parent", cascade = { CascadeType.ALL })
	private Collection<Render> children = new LinkedList<>();
	
	@ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.REFRESH)
	@JoinColumn(name = "render_setup_id")
	private RenderSetup setup;
	
	@Basic(optional = true)
	@Column(name = "percent_complete")
	private Integer percentComplete = 0;
	
	@Lob
	@Basic(optional = true, fetch = FetchType.LAZY)
	@Column(name = "png_base64", length = 4194304)
	private String pngBase64 = null;
	
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
	
	/**
	 * @return the offsetX
	 */
	public int getOffsetX() {
		
		return offsetX;
	}
	
	/**
	 * @param offsetX
	 *            the offsetX to set
	 */
	public void setOffsetX(int offsetX) {
		
		this.offsetX = offsetX;
	}
	
	/**
	 * @return the offsetY
	 */
	public int getOffsetY() {
		
		return offsetY;
	}
	
	/**
	 * @param offsetY
	 *            the offsetY to set
	 */
	public void setOffsetY(int offsetY) {
		
		this.offsetY = offsetY;
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
	
	public RenderSetup getSetup() {
		
		return setup;
	}
	
	public void setSetup(RenderSetup setup) {
		
		this.setup = setup;
	}
	
	public Integer getPercentComplete() {
		
		return percentComplete;
	}
	
	public Float getPercentCompleteFloat() {
		
		if (percentComplete == null)
			return null;
		return ((float) percentComplete) / 100f;
	}
	
	public void setPercentComplete(Integer percentComplete) {
		
		this.percentComplete = percentComplete;
	}
	
	public String getPngBase64() {
		
		return pngBase64;
	}
	
	public void setPngBase64(String pngBase64) {
		
		this.pngBase64 = pngBase64;
		this.result = null;
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
