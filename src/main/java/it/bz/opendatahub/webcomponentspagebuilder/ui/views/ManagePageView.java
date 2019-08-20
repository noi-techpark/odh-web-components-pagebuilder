package it.bz.opendatahub.webcomponentspagebuilder.ui.views;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageContent;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;
import it.bz.opendatahub.webcomponentspagebuilder.data.repositories.PageRepository;
import it.bz.opendatahub.webcomponentspagebuilder.rendering.PageRenderer;
import it.bz.opendatahub.webcomponentspagebuilder.ui.MainLayout;
import it.bz.opendatahub.webcomponentspagebuilder.ui.components.PageScreenshot;
import it.bz.opendatahub.webcomponentspagebuilder.ui.controllers.PublicationController;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.DuplicatePageDialog;
import it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs.DuplicatePageDialog.PageToDuplicate;

@Route(value = ManagePageView.ROUTE, layout = MainLayout.class)
@HtmlImport("frontend://styles/shared-styles.html")
public class ManagePageView extends VerticalLayout implements HasUrlParameter<String> {

	private static final long serialVersionUID = 295988085457653174L;

	public static final String ROUTE = "pages/manage";

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	PageRenderer pageRenderer;

	@Autowired
	PublicationController publicationController;

	@Autowired
	PageRepository pagesRepo;

	private Page page;

	private RouterLink label;

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

		HorizontalLayout contentLayout = new HorizontalLayout();
		contentLayout.setSpacing(true);
		contentLayout.setWidthFull();

		if (page.getDraftVersion() != null) {
			PageVersion pageVersion = page.getDraftVersion();

			Button previewButton = new Button("PREVIEW");
			previewButton.addClickListener(e -> {
				UI.getCurrent().getPage().executeJavaScript(
						String.format("window.open('/pages/preview/%s', '_blank');", pageVersion.getHash()));
			});

			Button editButton = new Button("EDIT");
			editButton.addClickListener(e -> {
				getUI().ifPresent(ui -> ui.navigate(EditPageVersionView.class, pageVersion.getIdAsString()));
			});

			Button publishButton = new Button("PUBLISH");
			publishButton.addClickListener(e -> {
				publicationController.publish(pageVersion, (updatedPage, updatedPageVersion) -> {
					page = updatedPage;

					refresh();
				});
			});

			Button discardButton = new Button("DISCARD");
			discardButton.addClickListener(e -> {
				publicationController.discard(pageVersion, (updatedPage) -> {
					page = updatedPage;

					refresh();
				});
			});

			Button duplicateButton = new Button("DUPLICATE");
			duplicateButton.addClickListener(e -> {
				DuplicatePageDialog dialog = applicationContext.getBean(DuplicatePageDialog.class);

				dialog.setBean(new PageToDuplicate(pageVersion));

				dialog.setSaveHandler((duplicatedPage) -> {
					getUI().ifPresent(ui -> ui.navigate(ManagePageView.class, duplicatedPage.getIdAsString()));

					Notification.show("Page duplicated!");
				});

				dialog.open();
			});

			HorizontalLayout buttons = new HorizontalLayout();
			buttons.add(previewButton);
			buttons.add(editButton);
			buttons.add(duplicateButton);
			buttons.add(publishButton);
			buttons.add(discardButton);

			VerticalLayout details = new VerticalLayout();
			details.setPadding(false);
			details.add(new Div(new Text(String.format("Updated at %s",
					pageVersion.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))))));
			details.add(buttons);

			PageScreenshot image = new PageScreenshot();
			image.setSrc(String.format("/pages/preview/%s.png?%s", pageVersion.getIdAsString(),
					DigestUtils.md5Hex(StringUtils.join(pageVersion.getContents().stream().map(PageContent::getMarkup)
							.collect(Collectors.toList())))));

			VerticalLayout layout = new VerticalLayout();
			layout.setPadding(false);
			layout.setWidth("640px");
			layout.add(image);
			layout.add(details);

			contentLayout.add(layout);
		} else {
			Button createButton = new Button("CREATE DRAFT");
			createButton.addClickListener(e -> {
				page.setDraftVersion(publicationController.createDraft(page));

				page = pagesRepo.save(page);

				refresh();

				Notification.show("Page draft created!");
			});

			VerticalLayout placeholder = new VerticalLayout();
			placeholder.addClassName("placeholder-contents");
			placeholder.setMargin(false);
			placeholder.setPadding(false);
			placeholder.setSpacing(true);

			placeholder.add(new Div(new Text(
					"There's currently no page draft defined. You can create one from scratch or by starting from the most recent published version.")));
			placeholder.add(createButton);

			HorizontalLayout layout = new HorizontalLayout();
			layout.addClassName("is-version-placeholder");
			layout.setWidth("640px");
			layout.add(placeholder);

			contentLayout.add(layout);
		}

		if (page.getPublicVersion() != null) {
			PageVersion pageVersion = page.getPublicVersion();

			HorizontalLayout buttons = new HorizontalLayout();

			Button previewButton = new Button("PREVIEW");
			previewButton.addClickListener(e -> {
				UI.getCurrent().getPage().executeJavaScript(
						String.format("window.open('/pages/preview/%s', '_blank');", pageVersion.getHash()));
			});

			buttons.add(previewButton);

			if (page.getPublication() != null) {
				Button visitButton = new Button("VISIT");
				visitButton.addClickListener(e -> {
					UI.getCurrent().getPage().executeJavaScript(
							String.format("window.open('https://%s', '_blank');", page.getPublication().getUri()));
				});

				buttons.add(visitButton);
			}

			Button unpublishButton = new Button("UNPUBLISH");
			unpublishButton.addClickListener(e -> {
				publicationController.unpublish(pageVersion, (updatedPage) -> {
					page = updatedPage;

					refresh();
				});
			});

			buttons.add(unpublishButton);

			Button duplicateButton = new Button("DUPLICATE");
			duplicateButton.addClickListener(e -> {
				DuplicatePageDialog dialog = applicationContext.getBean(DuplicatePageDialog.class);

				dialog.setBean(new PageToDuplicate(pageVersion));

				dialog.setSaveHandler((duplicatedPage) -> {
					getUI().ifPresent(ui -> ui.navigate(ManagePageView.class, duplicatedPage.getIdAsString()));

					Notification.show("Page duplicated!");
				});

				dialog.open();
			});

			buttons.add(duplicateButton);

			VerticalLayout details = new VerticalLayout();
			details.setPadding(false);
			details.add(new Div(new Text(String.format("Published at %s",
					pageVersion.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))))));
			details.add(buttons);

			PageScreenshot image = new PageScreenshot();
			image.setSrc(String.format("/pages/preview/%s.png?%s", pageVersion.getIdAsString(),
					DigestUtils.md5Hex(StringUtils.join(pageVersion.getContents().stream().map(PageContent::getMarkup)
							.collect(Collectors.toList())))));

			VerticalLayout layout = new VerticalLayout();
			layout.setPadding(false);
			layout.setWidth("640px");
			layout.add(image);
			layout.add(details);

			contentLayout.add(layout);
		} else {
			VerticalLayout placeholder = new VerticalLayout();
			placeholder.addClassName("placeholder-contents");
			placeholder.setMargin(false);
			placeholder.setPadding(false);
			placeholder.setSpacing(false);

			placeholder.add(new Div(new Text("There's no current public version for this page.")));

			HorizontalLayout layout = new HorizontalLayout();
			layout.addClassName("is-version-placeholder");
			layout.setWidth("640px");
			layout.add(placeholder);

			contentLayout.add(layout);
		}

		label = new RouterLink();

		Div labelWrapper = new Div(label);
		labelWrapper.addClassName("contains-page-label");

		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.addClassName("page-header");
		headerLayout.add(labelWrapper);

		add(headerLayout);
		add(contentLayout);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);

		label.setText(page.getLabel());
		label.setRoute(getUI().get().getRouter(), ManagePageView.class, page.getIdAsString());
	}

	private void refresh() {
		bind(page);
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

}
