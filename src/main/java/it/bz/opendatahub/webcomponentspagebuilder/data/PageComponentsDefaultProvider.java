package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.LinkedList;
import java.util.List;

/**
 * Default and simple implementation of a PageComponentsProvider
 */
public class PageComponentsDefaultProvider implements PageComponentsProvider {

	private List<PageComponent> availableComponents = new LinkedList<>();

	@Override
	public List<PageComponent> getAvailableComponents() {
		return availableComponents;
	}

	public void add(String title, String description, String asset, String tag, String defaultMarkup) {
		PageComponent component = new PageComponent();
		component.setTitle(title);
		component.setDescription(description);
		component.setAssetUrl(asset);
		component.setTag(tag);
		component.setDefaultMarkup(defaultMarkup);

		availableComponents.add(component);
	}

}
