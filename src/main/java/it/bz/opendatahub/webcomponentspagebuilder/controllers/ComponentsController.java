package it.bz.opendatahub.webcomponentspagebuilder.controllers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.bz.opendatahub.webcomponentspagebuilder.data.PageComponent;
import it.bz.opendatahub.webcomponentspagebuilder.data.PageComponentsProvider;

/**
 * Controller for getting the current list of web components that can be used on
 * pages. The available components are cached and refreshed automatically at
 * periodic intervals.
 * 
 * @author danielrampanelli
 */
@Component
public class ComponentsController {

	@Autowired(required = false)
	PageComponentsProvider componentsProvider;

	private Map<String, PageComponent> components = new HashMap<>();

	@PostConstruct
	private void postConstruct() {
		refresh();
	}

	public synchronized Collection<PageComponent> getAll() {
		return Collections.unmodifiableCollection(components.values());
	}

	public PageComponent getByUid(String uid) {
		return components.get(uid);
	}

	private void refresh() {
		List<PageComponent> fetchedComponents = new LinkedList<>();

		if (componentsProvider != null) {
			fetchedComponents.addAll(componentsProvider.getAvailableComponents());
		}

		synchronized (this) {
			components.clear();

			for (PageComponent pageComponent : fetchedComponents) {
				components.put(pageComponent.getUid(), pageComponent);
			}
		}
	}

	@Scheduled(cron = "0 0 0/3 * * ?")
	public void scheduleRefresh() {
		refresh();
	}

}
