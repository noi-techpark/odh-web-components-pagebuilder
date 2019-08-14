package it.bz.opendatahub.webcomponentspagebuilder.data.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@Entity
@Table(name = "pagebuilder_page_publication")
public class PagePublication extends BaseEntity {

	@OneToOne(fetch = FetchType.EAGER, mappedBy = "publication")
	private Page page;

	@Column(name = "domain_name")
	@NotNull
	private String domainName;

	@Column(name = "subdomain_name")
	private String subdomainName;

	@Column(name = "path_name")
	private String pathName;

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	@NotNull
	private PagePublicationStatus status;

	@Column(name = "deployed_datetime")
	@Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
	@NotNull
	private LocalDateTime deployedAt;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "version_id")
	@NotNull
	private PageVersion version;

	public PagePublication() {

	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getSubdomainName() {
		return subdomainName;
	}

	public void setSubdomainName(String subdomainName) {
		this.subdomainName = subdomainName;
	}

	public String getPathName() {
		return pathName;
	}

	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	public PageVersion getVersion() {
		return version;
	}

	public void setVersion(PageVersion version) {
		this.version = version;
	}

	public PagePublicationStatus getStatus() {
		return status;
	}

	public void setStatus(PagePublicationStatus status) {
		this.status = status;
	}

	public LocalDateTime getDeployedAt() {
		return deployedAt;
	}

	public void setDeployedAt(LocalDateTime deployedAt) {
		this.deployedAt = deployedAt;
	}

	public String getUri() {
		String hostName = getDomainName();

		if (getSubdomainName() != null && !getSubdomainName().equals("")) {
			hostName = String.format("%s.%s", getSubdomainName(), getDomainName());
		}

		if (getPathName() != null && !getPathName().equals("")) {
			return String.format("%s/%s", hostName, getPathName());
		}

		return hostName;
	}

}
