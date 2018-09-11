package org.snowjak.rays.frontend.ui.presentation.renderlist;

import java.util.LinkedList;
import java.util.List;

import com.vaadin.server.Resource;

public class RenderListItemBean {
	
	private String id;
	private String created;
	private String submitted;
	private String completed;
	private String size;
	private float percentComplete;
	private boolean isDecomposable, isSubmittable;
	private boolean hasResult;
	
	private boolean isOpen, isOpenable;
	private RenderListItemBean topLevelParent = null;
	private RenderListItemBean parent = null;
	private List<RenderListItemBean> children = new LinkedList<>();
	
	private Resource image;
	
	public RenderListItemBean() {
		
	}
	
	public RenderListItemBean(RenderListItemBean toCopy) {
		
		this.id = toCopy.id;
		this.created = toCopy.created;
		this.submitted = toCopy.submitted;
		this.completed = toCopy.completed;
		this.size = toCopy.size;
		this.percentComplete = toCopy.percentComplete;
		this.isDecomposable = toCopy.isDecomposable;
		this.isSubmittable = toCopy.isSubmittable;
		this.hasResult = toCopy.hasResult;
		this.isOpen = toCopy.isOpen;
		this.isOpenable = toCopy.isOpenable;
		this.topLevelParent = toCopy.topLevelParent;
		this.parent = toCopy.parent;
		this.children.addAll(toCopy.children);
	}
	
	public String getId() {
		
		return id;
	}
	
	public void setId(String id) {
		
		this.id = id;
	}
	
	public String getCreated() {
		
		return created;
	}
	
	public void setCreated(String created) {
		
		this.created = created;
	}
	
	public String getSubmitted() {
		
		return submitted;
	}
	
	public void setSubmitted(String submitted) {
		
		this.submitted = submitted;
	}
	
	public String getCompleted() {
		
		return completed;
	}
	
	public void setCompleted(String completed) {
		
		this.completed = completed;
	}
	
	public String getSize() {
		
		return size;
	}
	
	public void setSize(String size) {
		
		this.size = size;
	}
	
	public float getPercentComplete() {
		
		return percentComplete;
	}
	
	public void setPercentComplete(float percentComplete) {
		
		this.percentComplete = percentComplete;
	}
	
	public boolean isDecomposable() {
		
		return isDecomposable;
	}
	
	public void setDecomposable(boolean isDecomposed) {
		
		this.isDecomposable = isDecomposed;
	}
	
	public boolean isSubmittable() {
		
		return isSubmittable;
	}
	
	public void setSubmittable(boolean isSubmittable) {
		
		this.isSubmittable = isSubmittable;
	}
	
	public boolean isHasResult() {
		
		return hasResult;
	}
	
	public void setHasResult(boolean hasResult) {
		
		this.hasResult = hasResult;
	}
	
	public boolean isOpen() {
		
		return isOpen;
	}
	
	public void setOpen(boolean isOpen) {
		
		this.isOpen = isOpen;
	}
	
	public boolean isOpenable() {
		
		return isOpenable;
	}
	
	public void setOpenable(boolean isOpenable) {
		
		this.isOpenable = isOpenable;
	}
	
	public RenderListItemBean getTopLevelParent() {
		
		return topLevelParent;
	}
	
	public void setTopLevelParent(RenderListItemBean topLevelParent) {
		
		this.topLevelParent = topLevelParent;
	}
	
	public RenderListItemBean getParent() {
		
		return parent;
	}
	
	public void setParent(RenderListItemBean parent) {
		
		this.parent = parent;
	}
	
	public List<RenderListItemBean> getChildren() {
		
		return children;
	}
	
	public void setChildren(List<RenderListItemBean> children) {
		
		this.children = children;
	}
	
	public Resource getImage() {
		
		return image;
	}
	
	public void setImage(Resource image) {
		
		this.image = image;
	}
	
	@Override
	public int hashCode() {
		
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null || children.isEmpty()) ? 0 : children.hashCode());
		result = prime * result + ((completed == null) ? 0 : completed.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + (hasResult ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + (isDecomposable ? 1231 : 1237);
		result = prime * result + (isOpen ? 1231 : 1237);
		result = prime * result + (isOpenable ? 1231 : 1237);
		result = prime * result + (isSubmittable ? 1231 : 1237);
		result = prime * result + Float.floatToIntBits(percentComplete);
		result = prime * result + ((submitted == null) ? 0 : submitted.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RenderListItemBean other = (RenderListItemBean) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (completed == null) {
			if (other.completed != null)
				return false;
		} else if (!completed.equals(other.completed))
			return false;
		if (created == null) {
			if (other.created != null)
				return false;
		} else if (!created.equals(other.created))
			return false;
		if (hasResult != other.hasResult)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (isDecomposable != other.isDecomposable)
			return false;
		if (isOpen != other.isOpen)
			return false;
		if (isOpenable != other.isOpenable)
			return false;
		if (isSubmittable != other.isSubmittable)
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		}
		if (Float.floatToIntBits(percentComplete) != Float.floatToIntBits(other.percentComplete))
			return false;
		if (submitted == null) {
			if (other.submitted != null)
				return false;
		} else if (!submitted.equals(other.submitted))
			return false;
		return true;
	}
	
}