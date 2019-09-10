package it.bz.opendatahub.webcomponentspagebuilder.security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.server.ServletHelper.RequestType;
import com.vaadin.flow.shared.ApplicationConstants;

import it.bz.opendatahub.webcomponentspagebuilder.ui.errors.CustomRouteNotFoundError;
import it.bz.opendatahub.webcomponentspagebuilder.ui.views.AccessDeniedView;
import it.bz.opendatahub.webcomponentspagebuilder.ui.views.LoginView;

/**
 * List of security-related utilities.
 * 
 * @author danielrampanelli
 */
public class SecurityUtils {

	public static boolean isFrameworkInternalRequest(HttpServletRequest request) {
		final String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
		return parameterValue != null
				&& Stream.of(RequestType.values()).anyMatch(r -> r.getIdentifier().equals(parameterValue));
	}

	public static boolean isAccessGranted(Class<?> securedClass) {
		final boolean publicView = LoginView.class.equals(securedClass) || AccessDeniedView.class.equals(securedClass)
				|| CustomRouteNotFoundError.class.equals(securedClass);

		if (publicView) {
			return true;
		}

		Authentication userAuthentication = SecurityContextHolder.getContext().getAuthentication();

		if (!isUserLoggedIn(userAuthentication)) {
			return false;
		}

		Secured secured = AnnotationUtils.findAnnotation(securedClass, Secured.class);
		if (secured == null) {
			return true;
		}

		List<String> allowedRoles = Arrays.asList(secured.value());
		return userAuthentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.anyMatch(allowedRoles::contains);
	}

	public static boolean isUserLoggedIn() {
		return isUserLoggedIn(SecurityContextHolder.getContext().getAuthentication());
	}

	private static boolean isUserLoggedIn(Authentication authentication) {
		return authentication != null && !(authentication instanceof AnonymousAuthenticationToken);
	}

}
