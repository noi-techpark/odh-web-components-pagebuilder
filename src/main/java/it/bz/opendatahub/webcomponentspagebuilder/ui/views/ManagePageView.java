package it.bz.opendatahub.webcomponentspagebuilder.ui.views;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PagePublicationRepository;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;
import it.bz.opendatahub.webcomponentspagebuilder.events.PagePublicationUpdated;
import it.bz.opendatahub.webcomponentspagebuilder.events.PageVersionDeployedEvent;
import it.bz.opendatahub.webcomponentspagebuilder.events.PageVersionRemovedEvent;
import it.bz.opendatahub.webcomponentspagebuilder.events.PageVersionUndeployedEvent;
import it.bz.opendatahub.webcomponentspagebuilder.rendering.PageRenderer;
import it.bz.opendatahub.webcomponentspagebuilder.ui.MainLayout;
import it.bz.opendatahub.webcomponentspagebuilder.ui.components.PageScreenshot;
import it.bz.opendatahub.webcomponentspagebuilder.ui.controllers.PublishingController;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.DuplicatePageDialog;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.DuplicatePageDialog.PageToDuplicate;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.MonitorPagePublicationDialog;

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

	private class ActionsDialog extends Dialog {

		private static final long serialVersionUID = -6630088402796781147L;

		private VerticalLayout contents;

		public ActionsDialog() {
			setWidth("320px");

			contents = new VerticalLayout();
			contents.setMargin(false);

			add(contents);
		}

		public ActionsDialog withSection(String title, Button... buttons) {
			return withSection(title, null, buttons);
		}

		public ActionsDialog withSection(String title, String text, Button... buttons) {
			Div titleComponent = new Div();
			titleComponent.setText(title);

			Div textComponent = new Div();
			textComponent.setVisible(false);

			if (text != null) {
				textComponent.setText(text);
				textComponent.setVisible(true);
			}

			contents.add(titleComponent);
			contents.add(textComponent);
			contents.add(new HorizontalLayout(buttons));

			return this;
		}

		@SuppressWarnings("unused")
		public void addSection(String title, Button... buttons) {
			withSection(title, buttons);
		}

		@SuppressWarnings("unused")
		public void addSection(String title, String text, Button... buttons) {
			withSection(title, text, buttons);
		}

	}

	private class ManageDraftVersionComponent extends VerticalLayout {

		private static final long serialVersionUID = -3943724466695624512L;

		private VerticalLayout placeholder;

		private PageScreenshot image;

		private HorizontalLayout metadata;

		private Div metadataDatetime;

		private Optional<ActionsDialog> metadataActions = Optional.empty();

		private Div placeholderWrapper;

		public ManageDraftVersionComponent() {
			Button createButton = new Button("CREATE DRAFT");
			createButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
			createButton.addClickListener(e -> {
				Page freshPage = pagesRepo.getOne(page.getId());

				PageVersion draftVersion = publishingController.createDraft(freshPage);

				freshPage.setDraftVersion(draftVersion);
				freshPage.addVersion(draftVersion);

				page = pagesRepo.save(freshPage);

				onEdit();

				Notification.show("Page draft created!");
			});

			placeholder = new VerticalLayout();
			placeholder.addClassName("placeholder-contents");
			placeholder.setMargin(false);
			placeholder.setPadding(false);
			placeholder.setSpacing(false);
			placeholder.setWidth(null);
			placeholder.add("No page draft defined, create one from scratch or based on the current version.");
			placeholder.add(createButton);

			image = new PageScreenshot();
			image.setClickHandler(() -> onPreview());

			metadataDatetime = new Div();
			metadataDatetime.setText(LocalDateTime.now().toString());

			Button editButton = new Button();
			editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_PRIMARY);
			editButton.setIcon(VaadinIcon.EDIT.create());
			editButton.addClickListener(e -> onEdit());

			Button actionsButton = new Button();
			actionsButton.addThemeVariants(ButtonVariant.LUMO_ICON);
			actionsButton.setIcon(VaadinIcon.ELLIPSIS_DOTS_H.create());
			actionsButton.addClickListener(e -> onActions());

			metadata = new HorizontalLayout();
			metadata.setDefaultVerticalComponentAlignment(Alignment.CENTER);
			metadata.setWidthFull();
			metadata.add(metadataDatetime, new HorizontalLayout(editButton, actionsButton));
			metadata.expand(metadataDatetime);

			placeholderWrapper = new Div();
			placeholderWrapper.addClassName("placeholder-wrapper");
			placeholderWrapper.add(placeholder);

			add(new H2("Draft"));
			add(placeholderWrapper);
			add(image);
			add(metadata);

			setMargin(false);
			setPadding(true);
		}

		private void onActions() {
			metadataActions.ifPresent(dialog -> dialog.open());
		}

		private PageVersion getPageVersion() {
			return page.getDraftVersion();
		}

		private void onPreview() {
			metadataActions.ifPresent(dialog -> dialog.close());

			UI.getCurrent().getPage().executeJavaScript(
					String.format("window.open('/pages/preview/%s', '_blank');", getPageVersion().getHash()));
		}

		private void onEdit() {
			metadataActions.ifPresent(dialog -> dialog.close());

			getUI().ifPresent(ui -> ui.navigate(EditPageVersionView.class, getPageVersion().getIdAsString()));
		}

		private void onDuplicate() {
			metadataActions.ifPresent(dialog -> dialog.close());

			DuplicatePageDialog dialog = applicationContext.getBean(DuplicatePageDialog.class);

			dialog.setBean(new PageToDuplicate(getPageVersion()));

			dialog.setSaveHandler((duplicatedPage) -> {
				getUI().ifPresent(ui -> ui.navigate(ManagePageView.class, duplicatedPage.getIdAsString()));

				Notification.show("Page duplicated!");
			});

			dialog.open();
		}

		private void onPublish() {
			metadataActions.ifPresent(dialog -> dialog.close());

			if (publishingController.isLocked(page)) {
				new PageLockedDialog().open();
			} else {
				publishingController.publish(getPageVersion(), (updatedPage, updatedPageVersion) -> {
					page = updatedPage;

					refresh();
				});
			}
		}

		private void onDiscard() {
			metadataActions.ifPresent(dialog -> dialog.close());

			PageVersion pageVersion = getPageVersion();

			publishingController.discard(pageVersion, (updatedPage) -> {
				page = updatedPage;

				refresh();

				new Thread(() -> {
					eventPublisher.publishEvent(new PageVersionRemovedEvent(pageVersion));
				}).start();
			});
		}

		public void bind(Page page) {
			if (getPageVersion() != null) {
				removeClassName("is-version-placeholder");

				placeholderWrapper.setVisible(false);

				image.setVisible(true);

				image.setSrc(String.format("/pages/preview/%s.png?%s", getPageVersion().getIdAsString(),
						DigestUtils.md5Hex(StringUtils
								.join(getPageVersion().getContents().stream().filter(content -> content != null)
										.map(PageContent::getMarkup).collect(Collectors.toList())))));

				metadata.setVisible(true);

				metadataDatetime.setText(
						DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(getPageVersion().getUpdatedAt()));

				Button editButton = new Button("EDIT");
				editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
				editButton.addClickListener(e -> onEdit());

				Button publishButton = new Button("PUBLISH");
				publishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
				publishButton.addClickListener(e -> onPublish());

				Button discardButton = new Button("DISCARD");
				discardButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
				discardButton.addClickListener(e -> onDiscard());

				Button previewButton = new Button("PREVIEW");
				previewButton.addClickListener(e -> onPreview());

				Button duplicateButton = new Button("DUPLICATE");
				duplicateButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
				duplicateButton.addClickListener(e -> onDuplicate());

				metadataActions = Optional.of(new ActionsDialog().withSection("Contents", editButton, previewButton)
						.withSection("Publishing", publishButton, discardButton).withSection("Other", duplicateButton));
			} else {
				addClassName("is-version-placeholder");

				placeholderWrapper.setVisible(true);
				image.setVisible(false);
				metadata.setVisible(false);

				metadataActions = Optional.empty();
			}
		}

	}

	private class ManageCurrentVersionComponent extends VerticalLayout {

		private static final long serialVersionUID = -2059652706299048346L;

		private VerticalLayout placeholder;

		private PageScreenshot image;

		private HorizontalLayout metadata;

		private Optional<ActionsDialog> metadataActions = Optional.empty();

		private Div metadataComments;

		private Div metadataDatetime;

		private Div placeholderWrapper;

		public ManageCurrentVersionComponent() {
			placeholder = new VerticalLayout();
			placeholder.addClassName("placeholder-contents");
			placeholder.setMargin(false);
			placeholder.setPadding(false);
			placeholder.setSpacing(false);
			placeholder.setWidth(null);
			placeholder.add("There's no current version for this page.");

			image = new PageScreenshot();
			image.setClickHandler(() -> onPreview());

			metadataDatetime = new Div();
			metadataDatetime.setText(LocalDateTime.now().toString());

			Button actionsButton = new Button();
			actionsButton.addThemeVariants(ButtonVariant.LUMO_ICON);
			actionsButton.setIcon(VaadinIcon.ELLIPSIS_DOTS_H.create());
			actionsButton.addClickListener(e -> onActions());

			metadata = new HorizontalLayout();
			metadata.setDefaultVerticalComponentAlignment(Alignment.CENTER);
			metadata.setWidthFull();
			metadata.add(metadataDatetime, actionsButton);
			metadata.expand(metadataDatetime);

			metadataComments = new Div();

			placeholderWrapper = new Div();
			placeholderWrapper.addClassName("placeholder-wrapper");
			placeholderWrapper.add(placeholder);

			add(new H2("Current"));
			add(placeholderWrapper);
			add(image);
			add(metadata);
			add(metadataComments);

			setMargin(false);
			setPadding(true);
		}

		private PageVersion getPageVersion() {
			return page.getPublicVersion();
		}

		private void onActions() {
			metadataActions.ifPresent(actions -> actions.open());
		}

		private void onPreview() {
			metadataActions.ifPresent(dialog -> dialog.close());

			UI.getCurrent().getPage().executeJavaScript(
					String.format("window.open('/pages/preview/%s', '_blank');", getPageVersion().getHash()));
		}

		private void onPublish() {
			metadataActions.ifPresent(dialog -> dialog.close());

			if (publishingController.isLocked(page)) {
				new PageLockedDialog().open();
			} else {
				publishingController.deploy(getPageVersion(), (updatedPage) -> {
					page = updatedPage;

					refresh();

					new Thread(() -> {
						eventPublisher.publishEvent(new PageVersionDeployedEvent(getPageVersion()));
					}).start();
				});
			}
		}

		private void onDuplicate() {
			metadataActions.ifPresent(dialog -> dialog.close());

			DuplicatePageDialog dialog = applicationContext.getBean(DuplicatePageDialog.class);

			dialog.setBean(new PageToDuplicate(getPageVersion()));

			dialog.setSaveHandler((duplicatedPage) -> {
				getUI().ifPresent(ui -> ui.navigate(ManagePageView.class, duplicatedPage.getIdAsString()));

				Notification.show("Page duplicated!");
			});

			dialog.open();
		}

		public void bind(Page page) {
			PageVersion publicVersion = getPageVersion();

			if (publicVersion != null) {
				removeClassName("is-version-placeholder");

				placeholderWrapper.setVisible(false);

				image.setVisible(true);

				image.setSrc(String.format("/pages/preview/%s.png?%s", getPageVersion().getIdAsString(),
						DigestUtils.md5Hex(StringUtils
								.join(getPageVersion().getContents().stream().filter(content -> content != null)
										.map(PageContent::getMarkup).collect(Collectors.toList())))));

				metadata.setVisible(true);

				metadataDatetime.setText(
						DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(getPageVersion().getUpdatedAt()));

				if (!StringUtils.isEmpty(publicVersion.getComment())) {
					metadataComments.setText(publicVersion.getComment());
					metadataComments.setVisible(true);
				} else {
					metadataComments.setVisible(false);
				}

				Button previewButton = new Button("PREVIEW");
				previewButton.addClickListener(e -> onPreview());

				Button publishButton = new Button();
				publishButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
				publishButton.setText("PUBLISH");
				publishButton.addClickListener(e -> onPublish());

				Button duplicateButton = new Button("DUPLICATE");
				duplicateButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
				duplicateButton.addClickListener(e -> onDuplicate());

				metadataActions = Optional.of(new ActionsDialog().withSection("Contents", previewButton)
						.withSection("Publishing", publishButton).withSection("Other", duplicateButton));
			} else {
				addClassName("is-version-placeholder");

				placeholderWrapper.setVisible(true);
				image.setVisible(false);
				metadata.setVisible(false);

				metadataActions = Optional.empty();
			}
		}

	}

	private class ManagePublicVersionComponent extends VerticalLayout {

		private static final long serialVersionUID = 6714407780134549078L;

		private VerticalLayout undeployedStatus;

		private VerticalLayout pendingStatus;

		private VerticalLayout progressingStatus;

		private Div progressPercentage;

		private VerticalLayout completedStatus;

		private VerticalLayout disconnectedStatus;

		public ManagePublicVersionComponent() {
			Button abortButton = new Button();
			abortButton.addClickListener(e -> onAbort());
			abortButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
			abortButton.setText("ABORT");

			Button infosButton = new Button();
			infosButton.addClickListener(e -> onMoreDetails());
			infosButton.setText("MORE DETAILS");

			Button visitButton = new Button();
			visitButton.addClickListener(e -> onVisit());
			visitButton.setText("VISIT");

			Button unpublishButton = new Button();
			unpublishButton.addClickListener(e -> onUnpublish());
			unpublishButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
			unpublishButton.setText("UNPUBLISH");

			Icon disconnectedStatusIcon = VaadinIcon.PLUG.create();
			disconnectedStatusIcon.addClassName("icon");

			disconnectedStatus = new VerticalLayout();
			disconnectedStatus.addClassName("contains-status");
			disconnectedStatus.addClassName("contains-disconnected-status");
			disconnectedStatus.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
			disconnectedStatus.setMargin(false);
			disconnectedStatus.setPadding(false);
			disconnectedStatus.add(disconnectedStatusIcon);
			disconnectedStatus.add("No site connected to this page.");

			Icon undeployedStatusIcon = VaadinIcon.CLOUD_O.create();
			undeployedStatusIcon.addClassName("icon");

			undeployedStatus = new VerticalLayout();
			undeployedStatus.addClassName("contains-status");
			undeployedStatus.addClassName("contains-undeployed-status");
			undeployedStatus.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
			undeployedStatus.setMargin(false);
			undeployedStatus.setPadding(false);
			undeployedStatus.add(undeployedStatusIcon);
			undeployedStatus.add("No page version is currently published.");

			Icon pendingStatusIcon = VaadinIcon.HOURGLASS.create();
			pendingStatusIcon.addClassName("icon");

			pendingStatus = new VerticalLayout();
			pendingStatus.addClassName("contains-status");
			pendingStatus.addClassName("contains-pending-status");
			pendingStatus.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
			pendingStatus.setMargin(false);
			pendingStatus.setPadding(false);
			pendingStatus.add(pendingStatusIcon);
			pendingStatus.add("The deployment will be processed shortly, please wait...");
			pendingStatus.add(unpublishButton);

			progressPercentage = new Div();
			progressPercentage.addClassName("percentage");

			progressingStatus = new VerticalLayout();
			progressingStatus.addClassName("contains-status");
			progressingStatus.addClassName("contains-progressing-status");
			progressingStatus.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
			progressingStatus.setMargin(false);
			progressingStatus.setPadding(false);
			progressingStatus.add(progressPercentage);
			progressingStatus.add("The page version is currently being published...");
			progressingStatus.add(infosButton);

			Icon completedStatusIcon = VaadinIcon.CHECK_CIRCLE_O.create();
			completedStatusIcon.addClassName("icon");

			completedStatus = new VerticalLayout();
			completedStatus.addClassName("contains-status");
			completedStatus.addClassName("contains-completed-status");
			completedStatus.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
			completedStatus.setMargin(false);
			completedStatus.setPadding(false);
			completedStatus.add(completedStatusIcon);
			completedStatus.add("Current version has been published.");
			completedStatus.add(new HorizontalLayout(visitButton, unpublishButton));

			addClassName("contains-remote-page-part");

			Div statusWrappedContent = new Div();
			statusWrappedContent.addClassName("status-wrapped-content");
			statusWrappedContent.add(disconnectedStatus, undeployedStatus, pendingStatus, progressingStatus,
					completedStatus);

			Div statusWrapper = new Div();
			statusWrapper.addClassName("status-wrapper");
			statusWrapper.add(statusWrappedContent);

			add(new H2("Published"));
			add(statusWrapper);
		}

		private PageVersion getPageVersion() {
			return page.getPublicVersion();
		}

		private void onAbort() {
			PageVersion version = getPageVersion();
			PagePublication publication = version.getPublication();

			publishingController.abortPublication(publication);

			page = pagesRepo.getOne(page.getId());

			bind(page);
		}

		private void onUnpublish() {
			if (publishingController.isLocked(page)) {
				new PageLockedDialog().open();
			} else {
				publishingController.unpublish(getPageVersion(), (updatedPage) -> {
					page = updatedPage;

					refresh();

					new Thread(() -> {
						eventPublisher.publishEvent(new PageVersionUndeployedEvent(getPageVersion()));
					}).start();
				});
			}
		}

		private void onVisit() {
			UI.getCurrent().getPage()
					.executeJavaScript(String.format("window.open('https://%s', '_blank');", page.getSite().getUri()));
		}

		private void onMoreDetails() {
			PageVersion version = getPageVersion();
			PagePublication publication = version.getPublication();

			applicationContext.getBean(MonitorPagePublicationDialog.class).bind(publication).open();
		}

		public void bind(Page page) {
			if (page.getSite() != null) {
				disconnectedStatus.setVisible(false);
				undeployedStatus.setVisible(false);
				pendingStatus.setVisible(false);
				progressingStatus.setVisible(false);
				completedStatus.setVisible(false);

				PageVersion version = getPageVersion();
				PagePublication publication = version.getPublication();

				if (publication != null) {
					if (publication.isPending()) {
						pendingStatus.setVisible(true);
					}

					if (publication.isProgressing()) {
						publishingController.getTask(publication.getId()).ifPresent(task -> {
							progressPercentage
									.setText(String.format("%d %%", (int) Math.floor(task.getProgress() * 100)));

							progressingStatus.setVisible(true);
						});
					}

					if (publication.isCompleted()) {
						completedStatus.setVisible(true);
					}
				} else {
					undeployedStatus.setVisible(true);
				}
			} else {
				disconnectedStatus.setVisible(true);
				undeployedStatus.setVisible(false);
				pendingStatus.setVisible(false);
				progressingStatus.setVisible(false);
				completedStatus.setVisible(false);
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

	private ManageCurrentVersionComponent publicComponent;

	private ManagePublicVersionComponent remoteComponent;

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

		removeAll();

		draftComponent = new ManageDraftVersionComponent();
		draftComponent.addClassName("page-part");

		publicComponent = new ManageCurrentVersionComponent();
		publicComponent.addClassName("page-part");

		remoteComponent = new ManagePublicVersionComponent();
		remoteComponent.addClassName("page-part");

		HorizontalLayout contentLayout = new HorizontalLayout();
		contentLayout.setSpacing(true);
		contentLayout.setWidthFull();

		contentLayout.add(draftComponent);
		contentLayout.expand(draftComponent);

		contentLayout.add(publicComponent);
		contentLayout.expand(publicComponent);

		contentLayout.add(remoteComponent);
		contentLayout.expand(remoteComponent);

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
		remoteComponent.bind(page);
	}

	private void refresh() {
		draftComponent.bind(page);
		publicComponent.bind(page);
		remoteComponent.bind(page);
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
		Page freshPage = pagesRepo.getOne(page.getId());

		if (freshPage.getPublications().stream().filter(publication -> publication.getId().equals(event.getEntityID()))
				.count() > 0) {
			publicationsRepo.findById(event.getEntityID()).ifPresent(publication -> {
				page = freshPage;

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
