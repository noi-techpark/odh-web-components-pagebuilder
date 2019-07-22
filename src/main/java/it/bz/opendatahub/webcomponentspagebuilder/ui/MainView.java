package it.bz.opendatahub.webcomponentspagebuilder.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.olli.FileDownloadWrapper;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.StreamResource;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import it.bz.opendatahub.webcomponentspagebuilder.data.PageComponentsProvider;
import it.bz.opendatahub.webcomponentspagebuilder.templates.DefaultPage;

/**
 * Main UI of the page builder providing the page "canvas" and the tools for
 * creating or managing the web components
 */
@Route
@PWA(name = "OpenDataHub Web Components Page Builder", shortName = "Page Builder")
@HtmlImport("frontend://styles/shared-styles.html")
public class MainView extends VerticalLayout {

	private static final long serialVersionUID = -3885159105695452537L;

	@Autowired(required = false)
	PageComponentsProvider componentsProvider;

	private PageContentsArea contents;

	private Div components;

	public MainView() {
		contents = new PageContentsArea();

		DefaultPage page = new DefaultPage();
		page.setContents(contents);

		Div pageWrapper = new Div();
		pageWrapper.addClassName("page-wrapper");
		pageWrapper.setWidth("100%");
		pageWrapper.setHeight(null);
		pageWrapper.add(page);

		Button startOverButton = new Button("Start Over");
		startOverButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		startOverButton.addClickListener(e -> {
			// TODO ask for confirmation

			contents.clearContents();

			Notification.show("Page contents cleared.");
		});

		Button downloadButton = new Button("Download");
		downloadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		FileDownloadWrapper downloadButtonWrapper = new FileDownloadWrapper(
				new StreamResource("odh-webcomponents-page.html", () -> {
					try {
						return new ByteArrayInputStream(renderPage().getBytes());
					} catch (TemplateException | IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return null;
					}
				}));

		downloadButtonWrapper.wrapComponent(downloadButton);

		HorizontalLayout buttons = new HorizontalLayout(startOverButton, downloadButtonWrapper);
		buttons.addClassName("actions");
		buttons.setSpacing(true);

		components = new Div();
		components.addClassName("components");
		components.setSizeFull();
		components.setWidth("512px");

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

	@PostConstruct
	private void postConstruct() {
		if (componentsProvider != null) {
			componentsProvider.getAvailableComponents().forEach(availableComponent -> {
				components.add(new PageContentCard(availableComponent));
			});
		}
	}

	private String renderPage() throws TemplateException, IOException {
		List<PageContent> pageContents = contents.getChildren().filter(child -> child instanceof PageContent)
				.map(child -> (PageContent) child).collect(Collectors.toList());

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		Writer writer = new OutputStreamWriter(outputStream);

		Set<String> assets = new HashSet<>();
		List<String> components = new LinkedList<>();

		for (PageContent pageContent : pageContents) {
			if (pageContent.getAssetUrl() != null) {
				assets.add(pageContent.getAssetUrl());
			}

			components.add(pageContent.getCurrentMarkup());
		}

		Map<String, Object> input = new HashMap<String, Object>();
		input.put("assets", assets);
		input.put("contents", components);

		Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
		configuration.setClassForTemplateLoading(MainView.class,
				"/it/bz/opendatahub/webcomponentspagebuilder/templates");

		Template template = configuration.getTemplate("DefaultPage.ftl");
		template.process(input, writer);

		writer.close();

		return outputStream.toString();
	}

}
