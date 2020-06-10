package it.bz.opendatahub.webcomponentspagebuilder.data.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Entity for the definition of a site defined uniquely for all the various
 * deployments.
 * 
 * @author danielrampanelli
 */
@Entity
@Table(name = "pagebuilder_site")
public class Site extends BaseEntity {
	
	@Column(name = "subdomain_name")
	private String subdomainName;

	@Column(name = "domain_name")
	@NotNull
	private String domainName;

	@Column(name = "path_name")
	private String pathName;

	public Site() {

	}
	
	public String getSubdomainName() {
		return subdomainName;
	}
	
	public void setSubdomainName(String subdomainName) {
		this.subdomainName = subdomainName;
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

	public String getUri() {
		String hostName = getDomainName();

		if (getSubdomainName() != null && !getSubdomainName().equals("")) {
			hostName = String.format("%s.%s", getSubdomainName(), getDomainName());
		}

		if (getPathName() != null && !getPathName().equals("")) {
			return String.format("%s/%s/", hostName, getPathName());
		}

		return hostName;
	}

}
