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
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublication;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;
import it.bz.opendatahub.webcomponentspagebuilder.ui.MainLayout;
import it.bz.opendatahub.webcomponentspagebuilder.ui.controllers.PublishingController;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.CreatePageDialog;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.DangerAwareConfirmDialog;

/**
 * View for listing all currently active (non-archived) pages.
 * 
 * @author danielrampanelli
 */
@Route(value = PagesView.ROUTE, layout = MainLayout.class)
@HtmlImport("frontend://styles/shared-styles.html")
public class PagesView extends VerticalLayout {

	private static final long serialVersionUID = -3885159105695452537L;

	public static final String ROUTE = "";

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

			Button archiveButton = new Button();
			archiveButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
			archiveButton.setIcon(VaadinIcon.ARCHIVE.create());

			archiveButton.addClickListener(e -> {
				if (publishingController.isLocked(page)) {
					new PageLockedDialog().open();
				} else {
					DangerAwareConfirmDialog.create().withTitle("ARCHIVE")
							.withMessage("Are you sure you want to archive this page?").withAction("ARCHIVE")
							.withHandler(() -> {
								publishingController.archive(page, (updatedPage) -> {
									refresh();

									Notification.show("Page archived!");
								});
							}).open();
				}
			});

			return new HorizontalLayout(manageButton, archiveButton);
		})).setFlexGrow(0).setWidth("160px").setHeader("");

		grid.addColumn(Page::getLabel).setFlexGrow(1).setHeader("Label");
		
		grid.addColumn((page) -> {
			if (page.hasSite()) {
				return page.getSite().getUri();
			}
			
			return "";
		}).setFlexGrow(1).setHeader("Site");
		
		grid.addColumn(new ComponentRenderer<>((page) -> {
			HorizontalLayout cell = new HorizontalLayout();

			if (page.getDraftVersion() != null) {
				cell.add(VaadinIcon.CHECK.create());
			}

			return cell;
		})).setFlexGrow(0).setWidth("120px").setHeader("Draft");

		grid.addColumn(new ComponentRenderer<>((page) -> {
			HorizontalLayout cell = new HorizontalLayout();

			if (page.getPublicVersion() != null) {
				cell.add(VaadinIcon.CHECK.create());
			}

			return cell;
		})).setFlexGrow(0).setWidth("120px").setHeader("Current");

		grid.addColumn(new ComponentRenderer<>((page) -> {
			HorizontalLayout cell = new HorizontalLayout();

			if (page.getSite() != null) {
				PageVersion publicVersion = page.getPublicVersion();
				PagePublication publication = publicVersion.getPublication();

				if (publication != null) {
					if (publication.isPending()) {
						cell.add(VaadinIcon.HOURGLASS.create());
					}

					if (publication.isProgressing()) {
						cell.add(VaadinIcon.CLOUD_UPLOAD_O.create());
					}

					if (publication.isCompleted()) {
						cell.add(VaadinIcon.CHECK.create());
					}
				}
			}

			return cell;
		})).setFlexGrow(0).setWidth("120px").setHeader("Published");

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
