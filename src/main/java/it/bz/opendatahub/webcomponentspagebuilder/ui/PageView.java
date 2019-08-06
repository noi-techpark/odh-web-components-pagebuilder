package it.bz.opendatahub.webcomponentspagebuilder.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.olli.FileDownloadWrapper;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import freemarker.template.TemplateException;
import it.bz.opendatahub.webcomponentspagebuilder.data.Page;
import it.bz.opendatahub.webcomponentspagebuilder.data.PageComponentsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.data.PageRepository;
import it.bz.opendatahub.webcomponentspagebuilder.rendering.PageRenderer;

@Route("page")
@HtmlImport("frontend://styles/shared-styles.html")
public class PageView extends VerticalLayout implements HasUrlParameter<String> {

	private static final long serialVersionUID = 295988085457653174L;

	@Autowired
	PageRenderer pageRenderer;

	@Autowired
	PageRepository pageRepo;

	@Autowired(required = false)
	PageComponentsProvider componentsProvider;

	private Page page;

	private PageEditor editor;

	private Div components;

	private HorizontalLayout buttons;

	@PostConstruct
	private void postConstruct() {
		editor = new PageEditor();

		Div pageWrapper = new Div();
		pageWrapper.addClassName("page-wrapper");
		pageWrapper.setWidth("100%");
		pageWrapper.setHeight(null);
		pageWrapper.add(editor);

		Button saveButton = new Button("SAVE");
		saveButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		saveButton.addClickListener(e -> {
			page = pageRepo.save(page);

			editor.setPage(page);

			Notification.show("Page saved!");
		});

		Button previewButton = new Button("PREVIEW");
		previewButton.addClickListener(e -> {
			UI.getCurrent().getPage()
					.executeJavaScript(String.format("window.open('/preview/%s', '_blank');", page.getHash()));
		});

		buttons = new HorizontalLayout(saveButton, previewButton);
		buttons.addClassName("actions");
		buttons.setSpacing(true);

		components = new Div();
		components.addClassName("components");
		components.setSizeFull();
		components.setWidth("512px");

		if (componentsProvider != null) {
			componentsProvider.getAvailableComponents().forEach(availableComponent -> {
				components.add(new PageContentCard(availableComponent));
			});
		}

		VerticalLayout tools = new VerticalLayout();
		tools.addClassName("tools");
		tools.setMargin(true);
		tools.setSpacing(true);
		tools.add(buttons);
		tools.add(components);
		tools.setWidth(null);

		HorizontalLayout pageBuilder = new HorizontalLayout();
		pageBuilder.addClassName("page-builder");
		pageBuilder.add(pageWrapper);
		pageBuilder.add(tools);
		pageBuilder.setPadding(false);
		pageBuilder.setSpacing(true);
		pageBuilder.setSizeFull();
		add(pageBuilder);

		setMargin(false);
		setPadding(false);
		setSizeFull();
		setSpacing(false);
	}

	private void bind(Page pageToBind) {
		this.page = pageToBind;

		editor.bind(page);

		Button downloadButton = new Button("DOWNLOAD");
		downloadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		String fileName = String.format("%s.html", page.getDomainName());
		if (page.getPathName() != null && !page.getPathName().equals("")) {
			fileName = String.format("%s_%s.html", page.getDomainName(), page.getPathName());
		}

		FileDownloadWrapper downloadButtonWrapper = new FileDownloadWrapper(new StreamResource(fileName, () -> {
			try {
				return new ByteArrayInputStream(pageRenderer.renderPage(page).getBytes());
			} catch (TemplateException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			}
		}));

		downloadButtonWrapper.wrapComponent(downloadButton);

		buttons.add(downloadButtonWrapper);
	}

	@Override
	public void setParameter(BeforeEvent event, String uuid) {
		Optional<Page> page = pageRepo.findById(UUID.fromString(uuid));

		if (page.isPresent()) {
			bind(page.get());
		} else {
			// TODO show error message/label
		}
	}

}
