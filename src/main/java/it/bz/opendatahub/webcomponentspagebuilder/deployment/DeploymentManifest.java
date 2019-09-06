package it.bz.opendatahub.webcomponentspagebuilder.deployment;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublication;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;

/**
 * Object containing all required information for identifying and describing the
 * deployment's target and destination.
 * 
 * @author danielrampanelli
 */
public class DeploymentManifest {

	private String domainName;

	private String pathName;

	public DeploymentManifest(PageVersion pageVersion) {
		Page page = pageVersion.getPage();

		PagePublication pagePublication = page.getPublication();

		domainName = pagePublication.getDomainName();

		if (pagePublication.getSubdomainName() != null) {
			domainName = String.format("%s.%s", pagePublication.getSubdomainName(), domainName);
		}

		pagePublication.getPathName();
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
		if (pathName != null) {
			return String.format("%s/%s", domainName, pathName);
		}

		return domainName;
	}

}
