package it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublication;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublicationLog;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PagePublicationRepository;
import it.bz.opendatahub.webcomponentspagebuilder.deployment.DeploymentTask;
import it.bz.opendatahub.webcomponentspagebuilder.events.PagePublicationUpdated;
import it.bz.opendatahub.webcomponentspagebuilder.ui.controllers.PublishingController;

/**
 * Dialog that shows the realtime activity and logs for a single
 * {@link PagePublication}.
 * 
 * @author danielrampanelli
 */
@Scope("prototype")
@SpringComponent
@HtmlImport("styles/custom-base-dialog.html")
public class MonitorPagePublicationDialog extends BaseDialog {

	private static final long serialVersionUID = 2632015315367522924L;

	@Autowired
	PublishingController controller;

	@Autowired
	PagePublicationRepository repo;

	private UUID beanID;

	private TextField actionField;

	private TextField statusField;

	private TextField datetimeField;

	private TextField progressField;

	private Grid<PagePublicationLog> logsGrid;

	public MonitorPagePublicationDialog() {
		actionField = new TextField();
		actionField.setLabel("Action");
		actionField.setReadOnly(true);

		statusField = new TextField();
		statusField.setLabel("Status");
		statusField.setReadOnly(true);

		datetimeField = new TextField();
		datetimeField.setLabel("Date/Time");
		datetimeField.setReadOnly(true);

		progressField = new TextField();
		progressField.setLabel("Progress");
		progressField.setReadOnly(true);

		logsGrid = new Grid<>();
		logsGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT);
		logsGrid.setSizeFull();
		logsGrid.setSelectionMode(SelectionMode.NONE);

		logsGrid.addColumn(entry -> {
			return entry.getDatetime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		}).setHeader("Date/Time").setWidth("240px").setFlexGrow(0);

		logsGrid.addColumn(entry -> {
			return entry.getType().name();
		}).setHeader("Type").setWidth("100px").setFlexGrow(0);

		logsGrid.addColumn(entry -> {
			if (entry.getStackTrace() != null) {
				return String.format("%s<br/>%s", entry.getText(), entry.getStackTrace());
			}

			return entry.getText();
		}).setHeader("Text").setFlexGrow(1);

		getDialogContents().add(new FormLayout(actionField, statusField, datetimeField, progressField));

		getDialogContents().add(logsGrid);
		getDialogContents().expand(logsGrid);
	}

	@Override
	protected void configureSize() {
		setWidth("768px");
		setHeight("480px");
	}

	public MonitorPagePublicationDialog bind(PagePublication publication) {
		this.beanID = publication.getId();

		setDialogTitle(publication.getSite().getUri());

		update();

		return this;
	}

	private void update() {
		PagePublication bean = repo.getOne(beanID);

		Optional<DeploymentTask> task = controller.getTask(beanID);

		statusField.setValue(bean.getStatus().name());

		datetimeField.setValue(bean.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

		progressField.setValue("");

		task.ifPresent(t -> {
			progressField.setValue(String.format("%d %%", (int) Math.floor(t.getProgress() * 100)));
		});

		logsGrid.setItems(bean.getLogs());

		UI.getCurrent().getPage().executeJavaScript("$0._scrollToIndex($1)", logsGrid, bean.getLogs().size() - 1);
	}

	@Subscribe
	public void on(PagePublicationUpdated event) {
		if (event.isAbout(beanID)) {
			getUI().ifPresent(ui -> {
				ui.access(() -> {
					update();
					ui.push();
				});
			});
		}
	}

}