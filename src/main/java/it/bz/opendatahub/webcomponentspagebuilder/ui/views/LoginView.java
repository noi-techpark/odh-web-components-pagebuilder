package it.bz.opendatahub.webcomponentspagebuilder.ui.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

import it.bz.opendatahub.webcomponentspagebuilder.security.CustomHttpSessionRequestCache;
import it.bz.opendatahub.webcomponentspagebuilder.security.SecurityUtils;

@Route(value = LoginView.ROUTE)
@Viewport("width=device-width, minimum-scale=1, initial-scale=1, user-scalable=yes")
public class LoginView extends LoginOverlay implements AfterNavigationObserver, BeforeEnterObserver {

	private static final long serialVersionUID = -7642952436693765801L;

	public static final String ROUTE = "login";

	@Autowired
	public LoginView(AuthenticationManager authenticationManager, CustomHttpSessionRequestCache requestCache) {
		LoginI18n i18n = LoginI18n.createDefault();
		i18n.setHeader(new LoginI18n.Header());
		i18n.getHeader().setTitle("OPENDATAHUB PAGE BUILDER");
		i18n.setAdditionalInformation(null);
		i18n.setForm(new LoginI18n.Form());
		i18n.getForm().setSubmit("SIGN IN");
		i18n.getForm().setUsername("EMAIL");
		i18n.getForm().setPassword("PASSWORD");

		setI18n(i18n);
		setForgotPasswordButtonVisible(false);
		setOpened(true);

		addLoginListener(e -> {
			try {
				final Authentication authentication = authenticationManager
						.authenticate(new UsernamePasswordAuthenticationToken(e.getUsername(), e.getPassword()));

				SecurityContextHolder.getContext().setAuthentication(authentication);

				UI.getCurrent().navigate(requestCache.resolveRedirectUrl());
			} catch (AuthenticationException ex) {
				setError(true);
			}
		});
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		if (SecurityUtils.isUserLoggedIn()) {
			UI.getCurrent().getPage().getHistory().replaceState(null, "");
			event.rerouteTo(PagesView.class);
		}
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
	}

}