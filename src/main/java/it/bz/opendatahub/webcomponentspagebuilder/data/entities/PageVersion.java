package it.bz.opendatahub.webcomponentspagebuilder.data.entities;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

/**
 * Entity that holds the actual contents of a page and which can be either a
 * "draft" or a version that is currently deployed and accessible on the
 * internet.
 * 
 * @author danielrampanelli
 */
@Entity
@Table(name = "pagebuilder_page_version")
public class PageVersion extends BaseEntity {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "page_id")
	private Page page;

	@Column(name = "hash")
	@NotNull
	private String hash;

	@Column(name = "updated_datetime")
	@Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
	@NotNull
	private LocalDateTime updatedAt;

	@Column(name = "title")
	private String title;

	@Column(name = "description")
	private String description;

	@Column(name = "comment")
	private String comment;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "pageVersion", orphanRemoval = true)
	@OrderColumn(name = "position")
	private List<PageContent> contents = new LinkedList<>();

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "pageVersion", orphanRemoval = true)
	@OrderColumn(name = "position")
	private List<PageWidget> widgets = new LinkedList<>();

	public PageVersion() {

	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<PageContent> getContents() {
		return contents;
	}

	public void setContents(List<PageContent> contents) {
		this.contents = contents;
	}

	public List<PageWidget> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<PageWidget> widgets) {
		this.widgets = widgets;
	}

}
