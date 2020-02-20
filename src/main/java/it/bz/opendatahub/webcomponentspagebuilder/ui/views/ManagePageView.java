package it.bz.opendatahub.webcomponentspagebuilder.ui.views;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageContent;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublication;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublicationStatus;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PagePublicationRepository;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;
import it.bz.opendatahub.webcomponentspagebuilder.events.PagePublicationUpdated;
import it.bz.opendatahub.webcomponentspagebuilder.events.PageVersionRemovedEvent;
import it.bz.opendatahub.webcomponentspagebuilder.rendering.PageRenderer;
import it.bz.opendatahub.webcomponentspagebuilder.ui.MainLayout;
import it.bz.opendatahub.webcomponentspagebuilder.ui.components.PageScreenshot;
import it.bz.opendatahub.webcomponentspagebuilder.ui.controllers.PublishingController;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.DuplicatePageDialog;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.DuplicatePageDialog.PageToDuplicate;

/**
 * View for managing the versions of a single page, allowing therefore to
 * create, edit or remove draft and manage the publishing workflow.
 * 
 * @author danielrampanelli
 */
@Route(value = ManagePageView.ROUTE, layout = MainLayout.class)
@HtmlImport("frontend://styles/shared-styles.html")
public class ManagePageView extends VerticalLayout implements HasUrlParameter<String> {

	private static final long serialVersionUID = 295988085457653174L;

	public static final String ROUTE = "pages/manage";

	private class ManageDraftVersionComponent extends VerticalLayout {

		private static final long serialVersionUID = -3943724466695624512L;

		private VerticalLayout placeholder;

		private VerticalLayout defaultPlaceholder;

		private VerticalLayout archivePlaceholder;

		private PageScreenshot image;

		private Div details;

		private HorizontalLayout actions;

		private Button previewButton;

		private Button editButton;

		private Button duplicateButton;

		private Button publishButton;

		private Button discardButton;

		public ManageDraftVersionComponent() {
			Button createButton = new Button("CREATE DRAFT");
			createButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
			createButton.addClickListener(e -> {
				page.setDraftVersion(publishingController.createDraft(page));
				page.addVersion(page.getDraftVersion());

				page = pagesRepo.save(page);

				refresh();

				Notification.show("Page draft created!");
			});

			defaultPlaceholder = new VerticalLayout();
			defaultPlaceholder.add(
					"There's currently no page draft defined. You can create one from scratch or by starting from the most recent published version.");
			defaultPlaceholder.add(createButton);

			archivePlaceholder = new VerticalLayout();
			archivePlaceholder.add("There's no page draft defined.");

			placeholder = new VerticalLayout();
			placeholder.addClassName("placeholder-contents");
			placeholder.setMargin(false);
			placeholder.setPadding(false);
			placeholder.setSpacing(false);
			placeholder.add(defaultPlaceholder);
			placeholder.add(archivePlaceholder);

			image = new PageScreenshot();

			details = new Div();

			previewButton = new Button("PREVIEW");
			previewButton.addClickListener(e -> onPreview());

			editButton = new Button("EDIT");
			editButton.addClickListener(e -> onEdit());

			duplicateButton = new Button("DUPLICATE");
			duplicateButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
			duplicateButton.addClickListener(e -> onDuplicate());

			publishButton = new Button("PUBLISH");
			publishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			publishButton.addClickListener(e -> onPublish());

			discardButton = new Button("DISCARD");
			discardButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
			discardButton.addClickListener(e -> onDiscard());

			actions = new HorizontalLayout();
			actions.add(previewButton);
			actions.add(editButton);
			actions.add(duplicateButton);
			actions.add(publishButton);
			actions.add(discardButton);

			add(placeholder);
			add(image);
			add(details);
			add(actions);

			setMargin(false);
			setPadding(true);
			setWidth("640px");
		}

		private PageVersion getPageVersion() {
			return page.getDraftVersion();
		}

		private void onPreview() {
			UI.getCurrent().getPage().executeJavaScript(
					String.format("window.open('/pages/preview/%s', '_blank');", getPageVersion().getHash()));
		}

		private void onEdit() {
			getUI().ifPresent(ui -> ui.navigate(EditPageVersionView.class, getPageVersion().getIdAsString()));
		}

		private void onDuplicate() {
			DuplicatePageDialog dialog = applicationContext.getBean(DuplicatePageDialog.class);

			dialog.setBean(new PageToDuplicate(getPageVersion()));

			dialog.setSaveHandler((duplicatedPage) -> {
				getUI().ifPresent(ui -> ui.navigate(ManagePageView.class, duplicatedPage.getIdAsString()));

				Notification.show("Page duplicated!");
			});

			dialog.open();
		}

		private void onPublish() {
			publishingController.publish(getPageVersion(), (updatedPage, updatedPageVersion) -> {
				page = updatedPage;

				refresh();
			});
		}

		private void onDiscard() {
			PageVersion pageVersion = getPageVersion();

			publishingController.discard(pageVersion, (updatedPage) -> {
				page = updatedPage;

				refresh();

				eventPublisher.publishEvent(new PageVersionRemovedEvent(pageVersion));
			});
		}

		public void bind(Page page) {
			if (getPageVersion() != null) {
				removeClassName("is-version-placeholder");

				placeholder.setVisible(false);

				image.setVisible(true);
				details.setVisible(true);
				actions.setVisible(true);

				editButton.setVisible(!page.getArchived());
				publishButton.setVisible(!page.getArchived());
				discardButton.setVisible(!page.getArchived());

				image.setSrc(String.format("/pages/preview/%s.png?%s", getPageVersion().getIdAsString(),
						DigestUtils.md5Hex(StringUtils.join(getPageVersion().getContents().stream()
								.map(PageContent::getMarkup).collect(Collectors.toList())))));
			} else {
				addClassName("is-version-placeholder");

				placeholder.setVisible(true);
				defaultPlaceholder.setVisible(!page.getArchived());
				archivePlaceholder.setVisible(page.getArchived());

				image.setVisible(false);
				details.setVisible(false);
				actions.setVisible(false);
			}
		}

	}

	private class ManagePublicVersionComponent extends VerticalLayout {

		private static final long serialVersionUID = -2059652706299048346L;

		private VerticalLayout placeholder;

		private Button previewButton;

		private Button visitButton;

		private Button unpublishButton;

		private Button duplicateButton;

		private PageScreenshot image;

		private VerticalLayout task;

		private HorizontalLayout actions;

		private HorizontalLayout pendingTask;

		private HorizontalLayout progressingTask;

		private HorizontalLayout completedTask;

		public ManagePublicVersionComponent() {
			placeholder = new VerticalLayout();
			placeholder.addClassName("placeholder-contents");
			placeholder.setMargin(false);
			placeholder.setPadding(false);
			placeholder.setSpacing(false);
			placeholder.add("There's no current public version for this page.");

			image = new PageScreenshot();

			Div pendingTaskStatus = new Div();
			pendingTaskStatus.addClassName("task-status");
			pendingTaskStatus.setText("PENDING");

			Div pendingTaskStatusWrapper = new Div(pendingTaskStatus);
			pendingTaskStatusWrapper.addClassName("contains-task-status");
			pendingTaskStatusWrapper.setWidth("160px");

			Button dismissPendingButton = new Button();
			dismissPendingButton.addClickListener(e -> onDismissPending());
			dismissPendingButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
			dismissPendingButton.setText("Abort");

			VerticalLayout pendingTaskContents = new VerticalLayout();
			pendingTaskContents.setMargin(false);
			pendingTaskContents.setPadding(false);
			pendingTaskContents.setSpacing(false);
			pendingTaskContents.setWidthFull();
			pendingTaskContents.add("The deployment will be processed shortly, please wait...");
			pendingTaskContents.add(dismissPendingButton);

			pendingTask = new HorizontalLayout();
			pendingTask.addClassName("task");
			pendingTask.addClassName("is-pending");
			pendingTask.setWidthFull();
			pendingTask.add(pendingTaskStatusWrapper);
			pendingTask.add(pendingTaskContents);

			Div progressingTaskStatus = new Div();
			progressingTaskStatus.addClassName("task-status");
			progressingTaskStatus.setText("IN PROGRESS");

			Div progressingTaskStatusWrapper = new Div(progressingTaskStatus);
			progressingTaskStatusWrapper.addClassName("contains-task-status");
			progressingTaskStatusWrapper.setWidth("160px");

			progressingTask = new HorizontalLayout();
			progressingTask.addClassName("task");
			progressingTask.addClassName("is-progressing");
			progressingTask.setWidthFull();
			progressingTask.add(progressingTaskStatusWrapper);
			progressingTask.add("The page version is currently being deployed...");

			Div completedTaskStatus = new Div();
			completedTaskStatus.addClassName("task-status");
			completedTaskStatus.setText("DONE");

			Div completedTaskStatusWrapper = new Div(completedTaskStatus);
			completedTaskStatusWrapper.addClassName("contains-task-status");
			completedTaskStatusWrapper.setWidth("160px");

			completedTask = new HorizontalLayout();
			completedTask.addClassName("task");
			completedTask.addClassName("is-completed");
			completedTask.setWidthFull();
			completedTask.add(completedTaskStatusWrapper);
			completedTask.add("Public version has been deployed.");

			task = new VerticalLayout(pendingTask, progressingTask, completedTask);
			task.setMargin(false);
			task.setPadding(false);
			task.setSpacing(true);

			previewButton = new Button("PREVIEW");
			previewButton.addClickListener(e -> onPreview());

			visitButton = new Button("VISIT");
			visitButton.addClickListener(e -> onVisit());

			unpublishButton = new Button("UNPUBLISH");
			unpublishButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
			unpublishButton.addClickListener(e -> onUnpublish());

			duplicateButton = new Button("DUPLICATE");
			duplicateButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
			duplicateButton.addClickListener(e -> onDuplicate());

			actions = new HorizontalLayout();
			actions.add(previewButton);
			actions.add(visitButton);
			actions.add(unpublishButton);
			actions.add(duplicateButton);

			add(placeholder);
			add(image);
			// TODO add datetime label
			add(task);
			add(actions);

			setMargin(false);
			setPadding(true);
			setWidth("640px");
		}

		private void onDismissPending() {
			// TODO Auto-generated method stub
			Notification.show("Noy yet implemented!");
		}

		private void onPreview() {
			UI.getCurrent().getPage().executeJavaScript(
					String.format("window.open('/pages/preview/%s', '_blank');", getPageVersion().getHash()));
		}

		private void onVisit() {
			UI.getCurrent().getPage().executeJavaScript(
					String.format("window.open('https://%s', '_blank');", page.getPublication().getUri()));
		}

		private void onUnpublish() {
			publishingController.unpublish(getPageVersion(), (updatedPage) -> {
				page = updatedPage;

				refresh();

				eventPublisher.publishEvent(new PageVersionRemovedEvent(getPageVersion()));
			});
		}

		private void onDuplicate() {
			DuplicatePageDialog dialog = applicationContext.getBean(DuplicatePageDialog.class);

			dialog.setBean(new PageToDuplicate(getPageVersion()));

			dialog.setSaveHandler((duplicatedPage) -> {
				getUI().ifPresent(ui -> ui.navigate(ManagePageView.class, duplicatedPage.getIdAsString()));

				Notification.show("Page duplicated!");
			});

			dialog.open();
		}

		private PageVersion getPageVersion() {
			return page.getPublicVersion();
		}

		public void bind(Page page) {
			if (getPageVersion() != null) {
				removeClassName("is-version-placeholder");

				placeholder.setVisible(false);
				image.setVisible(true);
				task.setVisible(true);
				actions.setVisible(true);

				image.setSrc(String.format("/pages/preview/%s.png?%s", getPageVersion().getIdAsString(),
						DigestUtils.md5Hex(StringUtils.join(getPageVersion().getContents().stream()
								.map(PageContent::getMarkup).collect(Collectors.toList())))));

				PagePublication publication = page.getPublication();
				if (publication != null) {
					visitButton.setVisible(publication.getStatus().equals(PagePublicationStatus.COMPLETED));
					unpublishButton.setVisible(publication.getStatus().equals(PagePublicationStatus.COMPLETED));

					pendingTask.setVisible(publication.getStatus().equals(PagePublicationStatus.PENDING));
					progressingTask.setVisible(publication.getStatus().equals(PagePublicationStatus.PROGRESSING));
					completedTask.setVisible(publication.getStatus().equals(PagePublicationStatus.COMPLETED));
				} else {
					visitButton.setVisible(false);
					unpublishButton.setVisible(false);
				}
			} else {
				addClassName("is-version-placeholder");

				placeholder.setVisible(true);
				image.setVisible(false);
				task.setVisible(false);
				actions.setVisible(false);
			}
		}

	}

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@Autowired
	EventBus eventBus;

	@Autowired
	PageRenderer pageRenderer;

	@Autowired
	PublishingController publishingController;

	@Autowired
	PageRepository pagesRepo;

	@Autowired
	PagePublicationRepository publicationsRepo;

	private Page page;

	private Anchor pageTitle;

	private ManageDraftVersionComponent draftComponent;

	private ManagePublicVersionComponent publicComponent;

	@PostConstruct
	private void postConstruct() {
		addClassName("manage-page-view");
		setMargin(false);
		setPadding(true);
		setSizeFull();
		setSpacing(true);
	}

	private void bind(Page pageToBind) {
		this.page = pageToBind;

		draftComponent = new ManageDraftVersionComponent();
		publicComponent = new ManagePublicVersionComponent();

		HorizontalLayout contentLayout = new HorizontalLayout();
		contentLayout.setSpacing(true);
		contentLayout.setWidthFull();
		contentLayout.add(draftComponent);
		contentLayout.add(publicComponent);

		pageTitle = new Anchor();
		pageTitle.setText(page.getLabel());

		Div labelWrapper = new Div(pageTitle);
		labelWrapper.addClassName("contains-page-title");

		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.addClassName("page-header");
		headerLayout.add(labelWrapper);

		add(headerLayout);
		add(contentLayout);

		draftComponent.bind(page);
		publicComponent.bind(page);
	}

	private void refresh() {
		draftComponent.bind(page);
		publicComponent.bind(page);
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String uuid) {
		Optional<Page> page = pagesRepo.findById(UUID.fromString(uuid));

		if (page.isPresent()) {
			bind(page.get());
		} else {
			// TODO show error message/label
		}
	}

	@Subscribe
	public void on(PagePublicationUpdated event) {
		if (page.getPublication() != null && page.getPublication().getId().equals(event.getEntityID())) {
			publicationsRepo.findById(event.getEntityID()).ifPresent(publication -> {
				page.setPublication(publication);

				getUI().ifPresent(ui -> {
					ui.access(() -> {
						refresh();
						ui.push();
					});
				});
			});
		}
	}

}
