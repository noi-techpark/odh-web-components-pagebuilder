package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.List;

/**
 * Provider component exposing the list of {@link Domain} objects that are
 * available to users of the application.
 * 
 * @author danielrampanelli
 */
public interface DomainsProvider {

	public List<Domain> getAvailableDomains();

}
