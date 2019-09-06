package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Special implementation of a {@link PageComponentsProvider} that can be used
 * to combine multiple provider objects together.
 * 
 * @author danielrampanelli
 */
public class CompositePageComponentsProvider implements PageComponentsProvider {

	private List<PageComponentsProvider> providers = new LinkedList<>();

	@Override
	public List<PageComponent> getAvailableComponents() {
		List<PageComponent> pageComponents = new ArrayList<>();

		for (PageComponentsProvider provider : providers) {
			pageComponents.addAll(provider.getAvailableComponents());
		}

		return pageComponents;
	}

	public void add(PageComponentsProvider provider) {
		providers.add(provider);
	}

}
