package it.bz.opendatahub.webcomponentspagebuilder.controllers;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

	private Set<PageComponent> components = new HashSet<>();

	@PostConstruct
	private void postConstruct() {
		refresh();
	}

	public synchronized Set<PageComponent> getAll() {
		return Collections.unmodifiableSet(components);
	}

	private void refresh() {
		List<PageComponent> fetchedComponents = new LinkedList<>();

		if (componentsProvider != null) {
			fetchedComponents.addAll(componentsProvider.getAvailableComponents());
		}

		synchronized (this) {
			components.clear();
			components.addAll(fetchedComponents);
		}
	}

	@Scheduled(cron = "0 0 0/3 * * ?")
	public void scheduleRefresh() {
		refresh();
	}

}
