package it.bz.opendatahub.webcomponentspagebuilder.ui.views;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

import it.bz.opendatahub.webcomponentspagebuilder.ui.MainLayout;

/**
 * View that will be triggered when accessing views that require a valid
 * authentication or is restricted to users with specific permissions and
 * capabilities.
 * 
 * @author danielrampanelli
 */
@ParentLayout(MainLayout.class)
@Route
@Tag("access-denied-view")
public class AccessDeniedView extends PolymerTemplate<TemplateModel>
		implements HasErrorParameter<AccessDeniedException> {

	private static final long serialVersionUID = 2300765322091300456L;

	@Override
	public int setErrorParameter(BeforeEnterEvent beforeEnterEvent,
			ErrorParameter<AccessDeniedException> errorParameter) {
		return HttpServletResponse.SC_FORBIDDEN;
	}
}