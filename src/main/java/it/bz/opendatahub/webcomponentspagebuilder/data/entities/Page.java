package it.bz.opendatahub.webcomponentspagebuilder.data.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Entity acting as a container for different {@link PageVersion} entities,
 * currently modelled as a "draft" and "public" version.
 * 
 * @author danielrampanelli
 */
@Entity
@Table(name = "pagebuilder_page")
public class Page extends BaseEntity {

	@Column(name = "archived")
	@NotNull
	private Boolean archived = false;

	@Column(name = "label")
	@NotNull
	private String label;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "draft_version_id")
	private PageVersion draftVersion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "public_version_id")
	private PageVersion publicVersion;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "publication_id")
	private PagePublication publication;

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

	public PageVersion getDraftVersion() {
		return draftVersion;
	}

	public void setDraftVersion(PageVersion draftVersion) {
		this.draftVersion = draftVersion;
	}

	public PageVersion getPublicVersion() {
		return publicVersion;
	}

	public void setPublicVersion(PageVersion publicVersion) {
		this.publicVersion = publicVersion;
	}

	public PagePublication getPublication() {
		return publication;
	}

	public void setPublication(PagePublication publication) {
		this.publication = publication;
	}

}
