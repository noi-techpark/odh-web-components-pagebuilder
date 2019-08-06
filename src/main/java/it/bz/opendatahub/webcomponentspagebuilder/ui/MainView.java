package it.bz.opendatahub.webcomponentspagebuilder.ui;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.server.PWA;

import it.bz.opendatahub.webcomponentspagebuilder.data.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.PageRepository;

/**
 * Main UI of the page builder providing the page "canvas" and the tools for
 * creating or managing the web components
 */
@Route
@PWA(name = "OpenDataHub Web Components Page Builder", shortName = "Page Builder")
@HtmlImport("frontend://styles/shared-styles.html")
public class MainView extends VerticalLayout {

	private static final long serialVersionUID = -3885159105695452537L;

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	PageRepository pageRepo;

	private Grid<Page> grid;

	@PostConstruct
	private void postConstruct() {
		grid = new Grid<>();
		grid.setSizeFull();

		grid.addColumn(Page::getLabel).setFlexGrow(1).setHeader("PAGE");

		grid.addColumn(Page::getUri).setFlexGrow(1).setHeader("URL");

		grid.addColumn(new ComponentRenderer<>(page -> {
			Button previewButton = new Button("PREVIEW");
			previewButton.addClickListener(e -> {
				UI.getCurrent().getPage()
						.executeJavaScript(String.format("window.open('/preview/%s', '_blank');", page.getHash()));
			});

			Button editButton = new Button("EDIT");
			editButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
			editButton.addClickListener(e -> {
				getUI().ifPresent(ui -> ui.navigate(String.format("page/%s", page.getId().toString())));
			});

			Button archiveButton = new Button("ARCHIVE");
			archiveButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
			archiveButton.addClickListener(e -> {
				ConfirmDialog dialog = new ConfirmDialog("ARCHIVE", "Are you sure you want to archive this page?",
						"ARCHIVE", (dialogEvent) -> {
							page.setArchived(true);

							pageRepo.save(page);

							refresh();

							Notification.show("Page archived!");
						}, "CANCEL", (dialogEvent) -> {
							// noop
						});

				dialog.setConfirmButtonTheme("error primary");

				dialog.open();
			});

			return new HorizontalLayout(previewButton, editButton, archiveButton);
		})).setHeader("ACTIONS").setFlexGrow(0).setWidth("400px");

		add(new Button("NEW PAGE", e -> {
			CreatePageDialog dialog = applicationContext.getBean(CreatePageDialog.class);

			dialog.setSaveHandler((createPage) -> {
				getUI().ifPresent(ui -> ui.navigate(String.format("page/%s", createPage.getId().toString())));

				Notification.show("Page created!");
			});

			dialog.open();
		}));

		add(grid);

		setMargin(true);
		setPadding(false);
		setSizeFull();
		setSpacing(true);

		refresh();
	}

	public void refresh() {
		grid.setItems(pageRepo.findAllActive());
	}

}
