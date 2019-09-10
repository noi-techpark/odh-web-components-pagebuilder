package it.bz.opendatahub.webcomponentspagebuilder.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;

import it.bz.opendatahub.webcomponentspagebuilder.ui.views.LoginView;

/**
 * Component for storing the last accessed request, so that we can redirect the
 * user to the originally intended route after a successful login.
 * 
 * @author danielrampanelli
 */
public class LastRequestHttpSessionRequestCache extends HttpSessionRequestCache {

	@Override
	public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
		if (!SecurityUtils.isFrameworkInternalRequest(request)) {
			super.saveRequest(request, response);
		}
	}

	public String resolveRedirectUrl() {
		SavedRequest savedRequest = getRequest(VaadinServletRequest.getCurrent().getHttpServletRequest(),
				VaadinServletResponse.getCurrent().getHttpServletResponse());
		if (savedRequest instanceof DefaultSavedRequest) {
			final String requestURI = ((DefaultSavedRequest) savedRequest).getRequestURI();
			if (requestURI != null && !requestURI.isEmpty() && !requestURI.contains(LoginView.ROUTE)) {
				return requestURI.startsWith("/") ? requestURI.substring(1) : requestURI;
			}
		}

		return "";
	}

}