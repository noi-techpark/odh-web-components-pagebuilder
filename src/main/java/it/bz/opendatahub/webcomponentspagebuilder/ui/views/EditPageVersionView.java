package it.bz.opendatahub.webcomponentspagebuilder.ui.views;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import it.bz.opendatahub.webcomponentspagebuilder.controllers.ComponentsController;
import it.bz.opendatahub.webcomponentspagebuilder.data.PageComponent;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageWidget;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageVersionRepository;
import it.bz.opendatahub.webcomponentspagebuilder.events.PageVersionRemovedEvent;
import it.bz.opendatahub.webcomponentspagebuilder.events.PageVersionUpdatedEvent;
import it.bz.opendatahub.webcomponentspagebuilder.rendering.PageRenderer;
import it.bz.opendatahub.webcomponentspagebuilder.ui.MainLayout;
import it.bz.opendatahub.webcomponentspagebuilder.ui.components.PageComponentCard;
import it.bz.opendatahub.webcomponentspagebuilder.ui.components.PageComponentCard.ComponentActionsHandler;
import it.bz.opendatahub.webcomponentspagebuilder.ui.components.PageEditor;
import it.bz.opendatahub.webcomponentspagebuilder.ui.components.PageWidgetCard;
import it.bz.opendatahub.webcomponentspagebuilder.ui.components.PageWidgetCard.WidgetActionsHandler;
import it.bz.opendatahub.webcomponentspagebuilder.ui.controllers.PublishingController;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.PageElementConfigurationDialog;

/**
 * View for editing the contents (using drag-drop) and other related settings
 * for a single page version.
 * 
 * @author danielrampanelli
 */
@Route(value = EditPageVersionView.ROUTE, layout = MainLayout.class)
@HtmlImport("frontend://styles/shared-styles.html")
public class EditPageVersionView extends VerticalLayout
		implements HasUrlParameter<String>, ComponentActionsHandler, WidgetActionsHandler {

	private static final int MINIMUM_VISIBLE_DURATION_OF_SAVE = 500;

	private static final long serialVersionUID = 295988085457653174L;

	public static final String ROUTE = "pages/edit";

	@Autowired
	PageRenderer pageRenderer;

	@Autowired
	PublishingController publishingController;

	@Autowired
	PageRepository pagesRepo;

	@Autowired
	PageVersionRepository versionsRepo;

	@Autowired
	ComponentsController componentsController;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	private PageVersion pageVersion;

	private VerticalLayout widgetsLayout;

	private PageEditor editor;

	private Div availableComponents;

	private RouterLink pageTitle;

	private Binder<PageVersion> binder;

	private Div saveStatus;

	private Label widgetsPlaceholder;

	private VerticalLayout editorWrapper;

	private Tabs viewportSize;

	@PostConstruct
	private void postConstruct() {
		pageTitle = new RouterLink();

		Button previewButton = new Button("PREVIEW");
		previewButton.addClickListener(e -> {
			UI.getCurrent().getPage().executeJavaScript(
					String.format("window.open('/pages/preview/%s', '_blank');", pageVersion.getHash()));
		});

		Button publishButton = new Button("PUBLISH");
		publishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		publishButton.addClickListener(e -> {
			if (publishingController.isLocked(pageVersion.getPage())) {
				new PageLockedDialog().open();
			} else {
				publishingController.publish(pageVersion, (updatedPage, updatedPageVersion) -> {
					UI.getCurrent().navigate(ManagePageView.class, updatedPage.getIdAsString());
				});
			}
		});

		Button discardButton = new Button("DISCARD");
		discardButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		discardButton.addClickListener(e -> {
			publishingController.discard(pageVersion, (updatedPage) -> {
				UI.getCurrent().navigate(ManagePageView.class, updatedPage.getIdAsString());

				new Thread(() -> {
					eventPublisher.publishEvent(new PageVersionRemovedEvent(pageVersion));
				}).start();
			});
		});

		editor = new PageEditor();
		editor.setUpdateHandler(() -> save());

		Div pageTitleWrapper = new Div(pageTitle);
		pageTitleWrapper.addClassName("contains-page-title");

		HorizontalLayout headerButtons = new HorizontalLayout(previewButton, publishButton, discardButton);

		saveStatus = new Div();
		saveStatus.addClassName("contains-page-save-status");

		VerticalLayout pageTitleAndStatus = new VerticalLayout(pageTitleWrapper, saveStatus);
		pageTitleAndStatus.setMargin(false);
		pageTitleAndStatus.setPadding(false);
		pageTitleAndStatus.setSpacing(false);

		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.addClassName("page-header");
		headerLayout.setPadding(true);
		headerLayout.setWidthFull();

		headerLayout.add(pageTitleAndStatus);
		headerLayout.add(headerButtons);
		headerLayout.setFlexGrow(1, pageTitleAndStatus);
		headerLayout.setFlexGrow(0, headerButtons);

		editorWrapper = new VerticalLayout();
		editorWrapper.addClassName("page-editor-wrapper");
		editorWrapper.setMargin(false);
		editorWrapper.setPadding(true);
		editorWrapper.setSpacing(true);
		editorWrapper.setWidth("100%");
		editorWrapper.setHeight(null);
		editorWrapper.add(editor);
		editorWrapper.expand(editor);

		viewportSize = new Tabs();
		viewportSize.addThemeVariants(TabsVariant.LUMO_SMALL);
		viewportSize.add(new Tab(VaadinIcon.ARROWS_LONG_H.create(), new Span("Auto")));
		viewportSize.add(new Tab(VaadinIcon.MOBILE.create(), new Span("Mobile")));
		viewportSize.add(new Tab(VaadinIcon.TABLET.create(), new Span("Tablet")));
		viewportSize.add(new Tab(VaadinIcon.DESKTOP.create(), new Span("Desktop")));
		viewportSize.addSelectedChangeListener(e -> updateViewport());

		editorWrapper.add(viewportSize);
		editorWrapper.setHorizontalComponentAlignment(Alignment.CENTER, viewportSize);

		availableComponents = new Div();
		availableComponents.addClassName("components");
		availableComponents.setSizeFull();

		componentsController.getAll().stream().sorted((a, b) -> a.getTitle().compareTo(b.getTitle()))
				.forEach(availableComponent -> {
					availableComponents.add(new PageComponentCard(availableComponent, EditPageVersionView.this));
				});

		HorizontalLayout contentLayout = new HorizontalLayout();
		contentLayout.addClassName("contains-contents");
		contentLayout.setPadding(false);
		contentLayout.setSpacing(true);
		contentLayout.setSizeFull();

		Tab componentsTab = new Tab();
		componentsTab.add(VaadinIcon.CUBES.create());
		componentsTab.add("COMPONENTS");

		VerticalLayout library = new VerticalLayout();
		library.addClassName("contains-library");
		library.setMargin(false);
		library.setPadding(false);
		library.add(availableComponents);

		Tab widgetsTab = new Tab();
		widgetsTab.add(VaadinIcon.MAGIC.create());
		widgetsTab.add("WIDGETS");

		Tab metadataTab = new Tab();
		metadataTab.add(VaadinIcon.COGS.create());
		metadataTab.add("METADATA");

		TextField pageTitle = new TextField();
		pageTitle.setLabel("TITLE");
		pageTitle.setWidthFull();

		pageTitle.addValueChangeListener(e -> save());

		TextArea pageDescription = new TextArea();
		pageDescription.setLabel("DESCRIPTION");
		pageDescription.setWidthFull();

		pageDescription.addValueChangeListener(e -> save());

		binder = new Binder<>(PageVersion.class);
		binder.forField(pageTitle).bind("title");
		binder.forField(pageDescription).bind("description");

		VerticalLayout metadataLayout = new VerticalLayout();
		metadataLayout.setMargin(false);
		metadataLayout.setPadding(false);
		metadataLayout.setSpacing(true);
		metadataLayout.setWidthFull();
		metadataLayout.add(pageTitle);
		metadataLayout.add(pageDescription);

		widgetsLayout = new VerticalLayout();
		widgetsLayout.addClassName("contains-widgets");
		widgetsLayout.setMargin(false);
		widgetsLayout.setPadding(false);
		widgetsLayout.setSpacing(false);
		widgetsLayout.setWidthFull();

		widgetsPlaceholder = new Label("There are currently no widgets defined for this page.");
		widgetsPlaceholder.addClassName("placeholder");

		VerticalLayout widgetsWrapper = new VerticalLayout();
		widgetsWrapper.setMargin(false);
		widgetsWrapper.setPadding(false);
		widgetsWrapper.setSpacing(true);
		widgetsWrapper.add(widgetsLayout);
		widgetsWrapper.add(widgetsPlaceholder);

		widgetsPlaceholder.setVisible(false);

		Map<Tab, Component> tabsToPages = new HashMap<>();
		tabsToPages.put(componentsTab, library);
		tabsToPages.put(widgetsTab, widgetsWrapper);
		tabsToPages.put(metadataTab, metadataLayout);

		Tabs sidebarTabs = new Tabs();
		sidebarTabs.add(componentsTab);
		sidebarTabs.add(widgetsTab);
		sidebarTabs.add(metadataTab);
		sidebarTabs.setFlexGrowForEnclosedTabs(1);
		sidebarTabs.setWidthFull();

		sidebarTabs.addSelectedChangeListener(e -> {
			tabsToPages.values().forEach(page -> page.setVisible(false));
			tabsToPages.get(sidebarTabs.getSelectedTab()).setVisible(true);
		});

		tabsToPages.values().forEach(page -> page.setVisible(false));
		tabsToPages.get(sidebarTabs.getSelectedTab()).setVisible(true);

		Div sidebarComponents = new Div(library, widgetsWrapper, metadataLayout);
		sidebarComponents.addClassName("contains-sidebar-components");
		sidebarComponents.setSizeFull();

		VerticalLayout sidebar = new VerticalLayout();
		sidebar.addClassName("contains-sidebar");
		sidebar.setMargin(false);
		sidebar.setPadding(false);
		sidebar.setWidthFull();

		sidebar.add(sidebarTabs);
		sidebar.setFlexGrow(0, sidebarTabs);

		sidebar.add(sidebarComponents);
		sidebar.setFlexGrow(1, sidebarComponents);

		contentLayout.add(sidebar);
		contentLayout.add(editorWrapper);
		contentLayout.setFlexGrow(0, sidebar);
		contentLayout.setFlexGrow(1, editorWrapper);

		add(headerLayout);
		add(contentLayout);

		addClassName("edit-page-version-view");
		setMargin(false);
		setPadding(false);
		setSizeFull();
		setSpacing(false);
	}

	private void updateViewport() {
		editor.getElement().getClassList().remove("mobile");
		editor.getElement().getClassList().remove("tablet");
		editor.getElement().getClassList().remove("desktop");

		if (viewportSize.getSelectedIndex() == 1) {
			editor.getElement().getClassList().add("mobile");
		}

		if (viewportSize.getSelectedIndex() == 2) {
			editor.getElement().getClassList().add("tablet");
		}

		if (viewportSize.getSelectedIndex() == 3) {
			editor.getElement().getClassList().add("desktop");
		}
	}

	private void save() {
		new Thread(() -> {
			getUI().ifPresent(ui -> {
				ui.access(() -> {
					long time = -System.currentTimeMillis();

					saveStatus.setText("Saving...");

					ui.push();

					BinderValidationStatus<PageVersion> validation = binder.validate();
					if (validation.isOk()) {
						PageVersion bean = binder.getBean();
						bean.setUpdatedAt(LocalDateTime.now());

						pageVersion = versionsRepo.save(bean);

						binder.setBean(pageVersion);
						editor.setPageVersion(pageVersion);

						new Thread(() -> {
							eventPublisher.publishEvent(new PageVersionUpdatedEvent(pageVersion));
						}).start();
					}

					time += System.currentTimeMillis();

					if (time < MINIMUM_VISIBLE_DURATION_OF_SAVE) {
						try {
							Thread.sleep(MINIMUM_VISIBLE_DURATION_OF_SAVE - time);
						} catch (InterruptedException ee) {
							// noop
						}
					}

					saveStatus.setText(String.format("Saved on %s",
							pageVersion.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));

					ui.push();
				});
			});
		}).start();
	}

	@Override
	public void addComponentToContents(PageComponent component) {
		editor.addContent(component);
	}

	@Override
	public void addComponentToWidgets(PageComponent component) {
		PageWidget widget = editor.addWidget(component);

		save();

		widgetsLayout.add(new PageWidgetCard(component, widget, this));

		widgetsPlaceholder.setVisible(false);
	}

	@Override
	public void editWidget(PageWidget pageWidget) {
		List<PageWidget> matches = pageVersion.getWidgets().stream()
				.filter(widget -> widget.getWidgetID().equals(pageWidget.getWidgetID())).collect(Collectors.toList());

		if (!matches.isEmpty()) {
			PageWidget widget = matches.get(0);

			PageElementConfigurationDialog dialog = new PageElementConfigurationDialog(widget);

			dialog.setSaveHandler((updatedMarkup) -> {
				widget.setMarkup(updatedMarkup);

				editor.updateWidget(widget.getWidgetID(), updatedMarkup);

				save();
			});

			dialog.open();
		}
	}

	@Override
	public void removeWidget(PageWidget widget) {
		editor.removeWidget(widget);

		pageVersion.setWidgets(pageVersion.getWidgets().stream()
				.filter(content -> !content.getWidgetID().equals(widget.getWidgetID())).collect(Collectors.toList()));

		save();

		List<Component> components = widgetsLayout.getChildren().collect(Collectors.toList());
		for (Component component : components) {
			PageWidgetCard card = (PageWidgetCard) component;

			if (card.getWidget().getWidgetID().equals(widget.getWidgetID())) {
				widgetsLayout.remove(component);
			}
		}

		if (pageVersion.getWidgets().isEmpty()) {
			widgetsPlaceholder.setVisible(true);
		}
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);

		Page page = pageVersion.getPage();

		pageTitle.setText(page.getLabel());
		pageTitle.setRoute(getUI().get().getRouter(), ManagePageView.class, page.getIdAsString());
	}

	private void bind(PageVersion pageVersionToBind) {
		this.pageVersion = pageVersionToBind;

		editor.bind(pageVersion);
		binder.setBean(pageVersion);

		widgetsLayout.removeAll();

		for (PageWidget widget : pageVersion.getWidgets()) {
			PageComponent component = componentsController.getByUid(widget.getUid());
			if (component != null) {
				widgetsLayout.add(new PageWidgetCard(component, widget, this));
			}
		}

		if (pageVersion.getWidgets().isEmpty()) {
			widgetsPlaceholder.setVisible(true);
		}

		saveStatus.setText(String.format("Saved on %s",
				pageVersion.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String uuid) {
		Optional<PageVersion> pageVersion = versionsRepo.findById(UUID.fromString(uuid));

		if (pageVersion.isPresent()) {
			bind(pageVersion.get());
		} else {
			// TODO show error message/label
		}
	}

}
