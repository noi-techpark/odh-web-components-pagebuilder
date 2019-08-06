package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "pagebuilder_page")
public class Page extends BaseEntity {

	@Column(name = "archived")
	@NotNull
	private Boolean archived = false;

	@Column(name = "label")
	@NotNull
	private String label;

	@Column(name = "domain_name")
	@NotNull
	private String domainName;

	@Column(name = "path_name")
	private String pathName;

	@Column(name = "hash")
	@NotNull
	private String hash;

	@Column(name = "title")
	private String title;

	@Column(name = "description")
	private String description;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "page", orphanRemoval = true)
	@OrderColumn(name = "position")
	private List<PageContent> contents = new LinkedList<>();

	public Page() {

	}

	public Boolean getArchived() {
		return archived;
	}

	public void setArchived(Boolean archived) {
		this.archived = archived;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getPathName() {
		return pathName;
	}

	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<PageContent> getContents() {
		return contents;
	}

	public void setContents(List<PageContent> contents) {
		this.contents = contents;
	}

	public String getUri() {
		if (getPathName() != null && !getPathName().equals("")) {
			return String.format("%s/%s", getDomainName(), getPathName());
		}

		return getDomainName();
	}

}
