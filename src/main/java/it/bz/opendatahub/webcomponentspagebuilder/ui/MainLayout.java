package it.bz.opendatahub.webcomponentspagebuilder.ui;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.applayout.AbstractAppRouterLayout;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.AppLayoutMenu;
import com.vaadin.flow.component.applayout.AppLayoutMenuItem;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.server.PWA;

import it.bz.opendatahub.webcomponentspagebuilder.security.SecurityUtils;
import it.bz.opendatahub.webcomponentspagebuilder.ui.views.ArchiveView;
import it.bz.opendatahub.webcomponentspagebuilder.ui.views.DeploymentView;
import it.bz.opendatahub.webcomponentspagebuilder.ui.views.PagesView;

/**
 * Main layout of the application composed of a navigation bar and the content
 * of the currently active view.
 * 
 * @author danielrampanelli
 */
@PWA(name = "OpenDataHub Web Components Page Builder", shortName = "Page Builder", startPath = "login", enableInstallPrompt = false)
@Push
@HtmlImport("frontend://styles/shared-styles.html")
@Viewport("width=device-width, minimum-scale=1, initial-scale=1, user-scalable=yes")
public class MainLayout extends AbstractAppRouterLayout {

	private static final long serialVersionUID = 5502041174049673119L;

	@Override
	protected void configure(AppLayout appLayout, AppLayoutMenu menu) {
		appLayout.setBranding(new Span("PAGE BUILDER"));

		if (SecurityUtils.isUserLoggedIn()) {
			addMenuItem(menu, new AppLayoutMenuItem(VaadinIcon.FORM.create(), "PAGES", PagesView.ROUTE));

			addMenuItem(menu, new AppLayoutMenuItem(VaadinIcon.ARCHIVE.create(), "ARCHIVE", ArchiveView.ROUTE));

			addMenuItem(menu,
					new AppLayoutMenuItem(VaadinIcon.CLOUD_UPLOAD.create(), "DEPLOYMENT", DeploymentView.ROUTE));
		}
	}

	private void addMenuItem(AppLayoutMenu menu, AppLayoutMenuItem menuItem) {
		menuItem.getElement().setAttribute("theme", "icon-on-top");
		menu.addMenuItem(menuItem);
	}

	@Override
	protected void beforeNavigate(String route, HasElement content) {
		AppLayoutMenuItem selectedItem = getAppLayoutMenu().getSelectedMenuItem();
		if (selectedItem != null && !selectedItem.getRoute().equals(route)) {
			getAppLayoutMenu().selectMenuItem(null);
		}
	}

}
