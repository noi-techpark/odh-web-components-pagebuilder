package it.bz.opendatahub.webcomponentspagebuilder.ui.views;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;
import it.bz.opendatahub.webcomponentspagebuilder.ui.MainLayout;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.CreatePageDialog;

@Route(value = PagesView.ROUTE, layout = MainLayout.class)
@HtmlImport("frontend://styles/shared-styles.html")
public class PagesView extends VerticalLayout {

	private static final long serialVersionUID = -3885159105695452537L;

	public static final String ROUTE = "";

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	PageRepository pagesRepo;

	private Grid<Page> grid;

	@PostConstruct
	private void postConstruct() {
		grid = new Grid<>();
		grid.setSizeFull();

		grid.addColumn(Page::getLabel).setFlexGrow(1).setHeader("PAGE");

		// grid.addColumn(Page::getUri).setFlexGrow(1).setHeader("URL");

		grid.addColumn(new ComponentRenderer<>(page -> {
			Button manageButton = new Button("MANAGE");
			manageButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			manageButton.addClickListener(e -> {
				getUI().ifPresent(ui -> ui.navigate(ManagePageView.class, page.getId().toString()));
			});

			Button archiveButton = new Button("ARCHIVE");
			archiveButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
			archiveButton.addClickListener(e -> {
				ConfirmDialog dialog = new ConfirmDialog("ARCHIVE", "Are you sure you want to archive this page?",
						"ARCHIVE", (dialogEvent) -> {
							page.setArchived(true);
							page.setPublication(null);

							pagesRepo.save(page);

							refresh();

							Notification.show("Page archived!");
						}, "CANCEL", (dialogEvent) -> {
							// noop
						});

				dialog.setConfirmButtonTheme("error primary");

				dialog.open();
			});

			return new HorizontalLayout(manageButton, archiveButton);
		})).setHeader("ACTIONS").setFlexGrow(0).setWidth("320px");

		add(new Button("CREATE PAGE", e -> {
			CreatePageDialog dialog = applicationContext.getBean(CreatePageDialog.class);

			dialog.setSaveHandler((createPage) -> {
				getUI().ifPresent(ui -> ui.navigate(ManagePageView.class, createPage.getIdAsString()));

				Notification.show("Page created!");
			});

			dialog.open();
		}));

		add(grid);
		expand(grid);

		setMargin(false);
		setPadding(true);
		setSizeFull();
		setSpacing(true);

		refresh();
	}

	public void refresh() {
		grid.setItems(pagesRepo.findAllActive());
	}

}
