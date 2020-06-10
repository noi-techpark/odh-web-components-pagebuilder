package it.bz.opendatahub.webcomponentspagebuilder.data.entities;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import it.bz.opendatahub.webcomponentspagebuilder.data.converters.StringListAttributeConverter;

/**
 * Base class for elements that are present and will be rendered on a page,
 * regardless if it is part of the content area or a widget.
 * 
 * @author danielrampanelli
 */
@MappedSuperclass
public abstract class PageElement extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "page_version_id")
	private PageVersion pageVersion;

	@Column(name = "uid")
	@NotNull
	private String uid;

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

	public PageElement() {

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

}
