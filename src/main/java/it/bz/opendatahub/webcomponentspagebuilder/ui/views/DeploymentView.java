package it.bz.opendatahub.webcomponentspagebuilder.ui.views;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.eventbus.Subscribe;
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

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublication;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublicationStatus;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PagePublicationRepository;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageVersionRepository;
import it.bz.opendatahub.webcomponentspagebuilder.events.PagePublicationUpdated;
import it.bz.opendatahub.webcomponentspagebuilder.ui.MainLayout;
import it.bz.opendatahub.webcomponentspagebuilder.ui.controllers.PublishingController;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.DangerAwareConfirmDialog;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.MonitorPagePublicationDialog;

/**
 * View for listing all currently active (pending or progressing) page
 * publication/deployment tasks.
 * 
 * @author danielrampanelli
 */
@Route(value = DeploymentView.ROUTE, layout = MainLayout.class)
@HtmlImport("frontend://styles/shared-styles.html")
public class DeploymentView extends VerticalLayout {

	private static final long serialVersionUID = -278934974797139679L;

	public static final String ROUTE = "deployment";

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	PublishingController controller;

	@Autowired
	PageRepository pagesRepo;

	@Autowired
	PageVersionRepository versionsRepo;

	@Autowired
	PagePublicationRepository publicationsRepo;

	private Grid<PagePublication> grid;

	@PostConstruct
	private void postConstruct() {
		grid = new Grid<>();
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setSizeFull();

		grid.addColumn(new ComponentRenderer<>(publication -> {
			Button detailsButton = new Button();
			detailsButton.setIcon(VaadinIcon.INFO_CIRCLE_O.create());
			detailsButton.addThemeVariants(ButtonVariant.LUMO_ICON);

			detailsButton.addClickListener(e -> {
				applicationContext.getBean(MonitorPagePublicationDialog.class).bind(publication).open();
			});

			Button abortButton = new Button();
			abortButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
			abortButton.setEnabled(false);
			abortButton.setIcon(VaadinIcon.TRASH.create());

			abortButton.addClickListener(e -> {
				DangerAwareConfirmDialog.create().withTitle("ABORT")
						.withMessage("Are you sure you want to abort this page publication?").withAction("ABORT")
						.withHandler(() -> {
							controller.abortPublication(publication);

							refresh();

							Notification.show("Publication aborted.");
						}).open();
			});

			if (publication.getStatus().equals(PagePublicationStatus.PENDING)) {
				abortButton.setEnabled(true);
			}

			return new HorizontalLayout(detailsButton, abortButton);
		})).setFlexGrow(0).setWidth("160px").setHeader("");

		grid.addColumn(publication -> {
			return publication.getSite().getUri();
		}).setFlexGrow(1).setHeader("URL");

		grid.addColumn(publication -> {
			return publication.getAction().getLabel();
		}).setFlexGrow(0).setWidth("160px").setHeader("Action");

		grid.addColumn(publication -> {
			return publication.getStatus().getLabel();
		}).setFlexGrow(0).setWidth("160px").setHeader("Status");

		grid.addColumn(publication -> {
			return publication.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		}).setFlexGrow(0).setWidth("240px").setHeader("Date/Time");

		add(grid);
		expand(grid);

		setMargin(false);
		setPadding(true);
		setSizeFull();
		setSpacing(true);

		refresh();
	}

	public void refresh() {
		ArrayList<PagePublication> items = new ArrayList<>();
		items.addAll(publicationsRepo.findAllActive());
		items.addAll(publicationsRepo.findAllCompleted());

		grid.setItems(items);
	}

	@Subscribe
	public void on(PagePublicationUpdated event) {
		getUI().ifPresent(ui -> {
			ui.access(() -> {
				refresh();
				ui.push();
			});
		});
	}

}
