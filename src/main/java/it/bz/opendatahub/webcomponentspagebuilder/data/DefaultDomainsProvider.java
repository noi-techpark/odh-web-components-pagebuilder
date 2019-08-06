package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.LinkedList;
import java.util.List;

public class DefaultDomainsProvider implements DomainsProvider {

	private List<Domain> domains = new LinkedList<>();

	public DefaultDomainsProvider add(String hostName, Boolean subdomains) {
		Domain domain = new Domain();
		domain.setHostName(hostName);
		domain.setAllowSubdomains(subdomains);

		domains.add(domain);

		return this;
	}

	@Override
	public List<Domain> getAvailableDomains() {
		return domains;
	}

}
