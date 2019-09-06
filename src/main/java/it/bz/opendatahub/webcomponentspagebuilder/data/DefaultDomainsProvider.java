package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.LinkedList;
import java.util.List;

import it.bz.opendatahub.webcomponentspagebuilder.deployment.DeploymentPipeline;

/**
 * Default implementation of a {@link DomainsProvider} that exposes a
 * shortcut/helper method for adding {@link Domain} objects.
 * 
 * @author danielrampanelli
 */
public class DefaultDomainsProvider implements DomainsProvider {

	private List<Domain> domains = new LinkedList<>();

	public DefaultDomainsProvider add(String hostName, Boolean subdomains, DeploymentPipeline deploymentPipeline) {
		Domain domain = new Domain();
		domain.setHostName(hostName);
		domain.setAllowSubdomains(subdomains);
		domain.setDeploymentPipeline(deploymentPipeline);

		domains.add(domain);

		return this;
	}

	@Override
	public List<Domain> getAvailableDomains() {
		return domains;
	}

}
