package it.bz.opendatahub.webcomponentspagebuilder.ui.views;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;
import it.bz.opendatahub.webcomponentspagebuilder.ui.MainLayout;

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
	PageRepository pagesRepo;

	private Grid<Page> grid;

	@PostConstruct
	private void postConstruct() {
		grid = new Grid<>();
		grid.setSizeFull();

		grid.addColumn(Page::getLabel).setFlexGrow(1).setHeader("PAGE");

		grid.addColumn(new ComponentRenderer<>(page -> {
			Button manageButton = new Button("VIEW");
			manageButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			manageButton.addClickListener(e -> {
				getUI().ifPresent(ui -> ui.navigate(ManagePageView.class, page.getId().toString()));
			});

			return new HorizontalLayout(manageButton);
		})).setHeader("ACTIONS").setFlexGrow(0).setWidth("160px");

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
