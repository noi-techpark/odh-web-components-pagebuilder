package it.bz.opendatahub.webcomponentspagebuilder.security;

import org.springframework.security.access.AccessDeniedException;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;

import it.bz.opendatahub.webcomponentspagebuilder.ui.views.LoginView;

/**
 * Component for making sure the currently accessed view is accessed only by
 * authenticated or permitted users.
 * 
 * @author danielrampanelli
 */
@SpringComponent
public class SecurityAwareVaadinServiceInitListener implements VaadinServiceInitListener {

	private static final long serialVersionUID = 1135770617669991850L;

	@Override
	public void serviceInit(ServiceInitEvent event) {
		event.getSource().addUIInitListener(uiEvent -> {
			uiEvent.getUI().addBeforeEnterListener(this::beforeEnter);
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