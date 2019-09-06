package it.bz.opendatahub.webcomponentspagebuilder.ui.errors;

import javax.servlet.http.HttpServletResponse;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RouteNotFoundError;
import com.vaadin.flow.router.RouterLink;

import it.bz.opendatahub.webcomponentspagebuilder.ui.MainLayout;

/**
 * View that will be triggered when trying to access a route that is not mapped
 * to any views.
 * 
 * @author danielrampanelli
 */
@ParentLayout(MainLayout.class)
@HtmlImport("styles/shared-styles.html")
public class CustomRouteNotFoundError extends RouteNotFoundError {

	private static final long serialVersionUID = -7639517782733078760L;

	public CustomRouteNotFoundError() {
		RouterLink link = Component.from(ElementFactory.createRouterLink("", "Go to the front page."),
				RouterLink.class);
		getElement().appendChild(new Text("Oops you hit a 404. ").getElement(), link.getElement());
	}

	@Override
	public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
		return HttpServletResponse.SC_NOT_FOUND;
	}
}