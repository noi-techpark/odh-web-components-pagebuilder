package it.bz.opendatahub.webcomponentspagebuilder.data.entities;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import it.bz.opendatahub.webcomponentspagebuilder.data.converters.StringListAttributeConverter;

/**
 * Entity representing a web component placed on a {@link PageVersion} at a
 * defined position of the content.
 * 
 * @author danielrampanelli
 */
@Entity
@Table(name = "pagebuilder_page_content")
public class PageContent extends BaseEntity {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "page_version_id")
	private PageVersion pageVersion;

	@Column(name = "uid")
	@NotNull
	private String uid;

	@Column(name = "content_id")
	@NotNull
	private UUID contentID;

	@Column(name = "tag_name")
	@NotNull
	private String tagName;

	@Column(name = "assets")
	@NotNull
	@Convert(converter = StringListAttributeConverter.class)
	private List<String> assets = new LinkedList<>();

	@Column(name = "markup")
	@NotNull
	private String markup;

	@Column(name = "position")
	@NotNull
	private Integer position;

	public PageContent() {

	}

	public PageVersion getPageVersion() {
		return pageVersion;
	}

	public void setPageVersion(PageVersion pageVersion) {
		this.pageVersion = pageVersion;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public UUID getContentID() {
		return contentID;
	}

	public void setContentID(UUID contentID) {
		this.contentID = contentID;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public List<String> getAssets() {
		return assets;
	}

	public void setAssets(List<String> assets) {
		this.assets = assets;
	}

	public String getMarkup() {
		return markup;
	}

	public void setMarkup(String markup) {
		this.markup = markup;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public PageContent copy() {
		PageContent copy = new PageContent();
		copy.setTagName(getTagName());
		copy.setAssets(getAssets());
		copy.setMarkup(getMarkup());
		copy.setPosition(getPosition());

		return copy;
	}

}
