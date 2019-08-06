package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "pagebuilder_page_content")
public class PageContent extends BaseEntity {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "page_id")
	private Page page;

	@Column(name = "tag_name")
	@NotNull
	private String tagName;

	@Column(name = "assets")
	@NotNull
	@Type(type = "serializable")
	private List<String> assets = new LinkedList<>();

	@Column(name = "markup")
	@NotNull
	private String markup;

	@Column(name = "position")
	@NotNull
	private Integer position;

	public PageContent() {
		// TODO Auto-generated constructor stub
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
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
