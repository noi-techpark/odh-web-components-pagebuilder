package it.bz.opendatahub.webcomponentspagebuilder.security;

import org.springframework.security.access.AccessDeniedException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;

import it.bz.opendatahub.webcomponentspagebuilder.ui.views.LoginView;

@SpringComponent
public class CustomVaadinServiceInitListener implements VaadinServiceInitListener {

	private static final long serialVersionUID = 1135770617669991850L;

	@Override
	public void serviceInit(ServiceInitEvent event) {
		event.getSource().addUIInitListener(uiEvent -> {
			final UI ui = uiEvent.getUI();
			// TODO configure offline banner/message
			ui.addBeforeEnterListener(this::beforeEnter);
		});
	}

	private void beforeEnter(BeforeEnterEvent event) {
		final boolean accessGranted = SecurityUtils.isAccessGranted(event.getNavigationTarget());
		if (!accessGranted) {
			if (SecurityUtils.isUserLoggedIn()) {
				event.rerouteToError(AccessDeniedException.class);
			} else {
				event.rerouteTo(LoginView.class);
			}
		}
	}
}