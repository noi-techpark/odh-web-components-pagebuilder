package it.bz.opendatahub.webcomponentspagebuilder.ui.views;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;
import it.bz.opendatahub.webcomponentspagebuilder.ui.MainLayout;
import it.bz.opendatahub.webcomponentspagebuilder.ui.controllers.PublishingController;

/**
 * View for listing all archived pages.
 * 
 * @author danielrampanelli
 */
@Route(value = ArchiveView.ROUTE, layout = MainLayout.class)
@HtmlImport("frontend://styles/shared-styles.html")
public class ArchiveView extends VerticalLayout {

	private static final long serialVersionUID = 4125443979779370724L;

	public static final String ROUTE = "archive";

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	PublishingController publishingController;

	@Autowired
	PageRepository pagesRepo;

	private Grid<Page> grid;

	@PostConstruct
	private void postConstruct() {
		grid = new Grid<>();
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setSizeFull();

		grid.addColumn(new ComponentRenderer<>(page -> {
			Button manageButton = new Button();
			manageButton.addThemeVariants(ButtonVariant.LUMO_ICON);
			manageButton.setIcon(VaadinIcon.EDIT.create());

			manageButton.addClickListener(e -> {
				getUI().ifPresent(ui -> ui.navigate(ManagePageView.class, page.getId().toString()));
			});

			Button restoreButton = new Button();
			restoreButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_ICON);
			restoreButton.setIcon(VaadinIcon.REPLY.create());

			restoreButton.addClickListener(e -> {
				if (publishingController.isLocked(page)) {
					new PageLockedDialog().open();
				} else {
					publishingController.restore(page, (duplicatedPage) -> {
						getUI().ifPresent(ui -> ui.navigate(ManagePageView.class, duplicatedPage.getIdAsString()));
						
						Notification.show("Page restored!");
					});
				}
			});

			return new HorizontalLayout(manageButton, restoreButton);
		})).setFlexGrow(0).setWidth("160px").setHeader("");

		grid.addColumn(Page::getLabel).setFlexGrow(1).setHeader("Label");

		add(grid);
		expand(grid);

		setMargin(false);
		setPadding(true);
		setSizeFull();
		setSpacing(true);

		refresh();
	}

	public void refresh() {
		grid.setItems(pagesRepo.findAllArchived());
	}

}
