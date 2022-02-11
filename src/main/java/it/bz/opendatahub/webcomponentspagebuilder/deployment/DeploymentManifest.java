package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Site;

/**
 * Object containing all required information for identifying and describing the
 * deployment's target and destination.
 * 
 * @author danielrampanelli
 */
public class DeploymentManifest {

	private String domainName;

	private String pathName;

	public DeploymentManifest() {

	}

	public DeploymentManifest(PageVersion pageVersion) {
		Page page = pageVersion.getPage();

		Site site = page.getSite();

		if (site.getSubdomainName() != null) {
			setDomainName(String.format("%s.%s", site.getSubdomainName(), site.getDomainName()));
		} else {
			setDomainName(site.getDomainName());
		}

		setPathName(site.getPathName());
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public DeploymentManifest withDomainName(String domainName) {
		setDomainName(domainName);
		return this;
	}

	public String getPathName() {
		return pathName;
	}

	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	public DeploymentManifest withPathName(String pathName) {
		setPathName(pathName);
		return this;
	}

	public String getUri() {
		if (pathName != null) {
			return String.format("%s/%s", domainName, pathName);
		}

		return domainName;
	}

}
