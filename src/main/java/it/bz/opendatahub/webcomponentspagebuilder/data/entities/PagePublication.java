package it.bz.opendatahub.webcomponentspagebuilder.data.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

/**
 * Entity for the definition of the publication of a page (actually a specific
 * {@link PageVersion}).
 * 
 * @author danielrampanelli
 */
@Entity
@Table(name = "pagebuilder_page_publication")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class PagePublication extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "page_id")
	@NotNull
	private Page page;

	@OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH })
	@JoinColumn(name = "version_id")
	@NotNull
	private PageVersion version;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_id")
	@NotNull
	private Site site;

	@Column(name = "action")
	@Enumerated(EnumType.STRING)
	@NotNull
	private PagePublicationAction action;

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	@NotNull
	private PagePublicationStatus status;

	@Column(name = "updated_at")
	@Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
	@NotNull
	private LocalDateTime updatedAt;

	@Column(name = "logs")
	@Type(type = "jsonb")
	@NotNull
	private List<PagePublicationLog> logs = new ArrayList<>();

	public PagePublication() {

	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public PageVersion getVersion() {
		return version;
	}

	public void setVersion(PageVersion version) {
		this.version = version;
	}

	public Site getSite() {
		return site;
	}

	public boolean hasSite() {
		return site != null;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public PagePublicationAction getAction() {
		return action;
	}

	public void setAction(PagePublicationAction action) {
		this.action = action;
	}

	public PagePublicationStatus getStatus() {
		return status;
	}

	public void setStatus(PagePublicationStatus status) {
		this.status = status;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<PagePublicationLog> getLogs() {
		return logs;
	}

	public void setLogs(List<PagePublicationLog> logs) {
		this.logs = logs;
	}

	public void add(PagePublicationLog entry) {
		getLogs().add(entry);
	}

	public boolean isPending() {
		return status != null && status.equals(PagePublicationStatus.PENDING);
	}

	public boolean isProgressing() {
		return status != null && status.equals(PagePublicationStatus.PROGRESSING);
	}

	public boolean isCompleted() {
		return status != null && status.equals(PagePublicationStatus.COMPLETED);
	}

}
