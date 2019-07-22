package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.List;

/**
 * Provides the list of currently available web components that can be placed on a page
 */
public interface PageComponentsProvider {

	public List<PageComponent> getAvailableComponents();

}
