package it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs;

import java.util.Optional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageElement;
import it.bz.opendatahub.webcomponentspagebuilder.ui.components.PageElementMarkupField;
import it.bz.opendatahub.webcomponentspagebuilder.ui.components.PageElementPreview;

/**
 * Dialog for managing the markup of a selected page content, which allows
 * editing or reverting to the default markup.
 * 
 * @author danielrampanelli
 */
@HtmlImport("styles/page-element-preview-dialog.html")
public class PageElementConfigurationDialog extends Dialog {

	private static final long serialVersionUID = -131927632298787518L;

	@FunctionalInterface
	public interface SaveHandler {
		public void save(String markup);
	}

	public class PageElementToUpdate {

		private String markup;

		public PageElementToUpdate() {

		}

		public String getMarkup() {
			return markup;
		}

		public void setMarkup(String markup) {
			this.markup = markup;
		}

	}

	private Optional<SaveHandler> saveHandler = Optional.empty();
	private Tabs viewportSize;
	private PageElementPreview preview;

	public PageElementConfigurationDialog(PageElement pageElement) {
		Div title = new Div();
		title.addClassName("dialog-title");
		title.setText(pageElement.getTagName());

		Button fullscreenButton = new Button();
		fullscreenButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
		fullscreenButton.setIcon(VaadinIcon.EXPAND_SQUARE.create());

		fullscreenButton.addClickListener(e -> {
			if (getElement().getAttribute("theme").equals("page-element-preview-dialog-fullscreen")) {
				getElement().setAttribute("theme", "page-element-preview-dialog");
				fullscreenButton.setIcon(VaadinIcon.EXPAND_SQUARE.create());
			} else {
				getElement().setAttribute("theme", "page-element-preview-dialog-fullscreen");
				fullscreenButton.setIcon(VaadinIcon.COMPRESS_SQUARE.create());
			}
		});

		Button closeButton = new Button(VaadinIcon.CLOSE.create());
		closeButton.addClickListener(e -> close());
		closeButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);

		HorizontalLayout headButtons = new HorizontalLayout();
		headButtons.setSpacing(false);
		headButtons.add(fullscreenButton);
		headButtons.add(closeButton);

		HorizontalLayout headPart = new HorizontalLayout();
		headPart.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		headPart.setWidthFull();
		headPart.add(title);
		headPart.expand(title);
		headPart.add(headButtons);

		PageElementMarkupField markupField = new PageElementMarkupField();
		markupField.setText(pageElement.getMarkup());

		preview = new PageElementPreview();
		preview.updateElement(pageElement, pageElement.getMarkup());

		viewportSize = new Tabs();
		viewportSize.addThemeVariants(TabsVariant.LUMO_SMALL);
		viewportSize.add(new Tab(VaadinIcon.ARROWS_LONG_H.create(), new Span("Auto")));
		viewportSize.add(new Tab(VaadinIcon.MOBILE.create(), new Span("Mobile")));
		viewportSize.add(new Tab(VaadinIcon.TABLET.create(), new Span("Tablet")));
		viewportSize.add(new Tab(VaadinIcon.DESKTOP.create(), new Span("Desktop")));
		viewportSize.addSelectedChangeListener(e -> updateViewport());

		markupField.setUpdateHandler((markup) -> {
			preview.updateElement(pageElement, markup);
		});

		VerticalLayout previewWrapper = new VerticalLayout();
		previewWrapper.addClassName("preview-wrapper");
		previewWrapper.setMargin(false);
		previewWrapper.setPadding(true);
		previewWrapper.setSpacing(true);
		previewWrapper.setWidth(null);
		previewWrapper.setHeight(null);
		previewWrapper.add(preview);
		previewWrapper.expand(preview);

		previewWrapper.add(viewportSize);
		previewWrapper.setHorizontalComponentAlignment(Alignment.CENTER, viewportSize);

		HorizontalLayout contents = new HorizontalLayout();
		contents.setSizeFull();
		contents.add(markupField);
		contents.expand(markupField);
		contents.add(previewWrapper);
		contents.expand(previewWrapper);

		Button saveButton = new Button("SAVE");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(clickEvent -> {
			saveHandler.ifPresent(handler -> handler.save(markupField.getText()));
			close();
		});

		VerticalLayout wrapperPart = new VerticalLayout();
		wrapperPart.addClassName("dialog-wrapper");
		wrapperPart.setMargin(false);
		wrapperPart.setPadding(true);
		wrapperPart.setSizeFull();
		wrapperPart.add(contents);
		wrapperPart.add(saveButton);
		wrapperPart.setHorizontalComponentAlignment(Alignment.END, saveButton);

		VerticalLayout layout = new VerticalLayout();
		layout.addClassName("page-element-preview-dialog");
		layout.setMargin(false);
		layout.setPadding(false);
		layout.setSpacing(false);
		layout.setSizeFull();
		layout.add(headPart);
		layout.add(wrapperPart);

		add(layout);
		getElement().setAttribute("theme", "page-element-preview-dialog");
		setSizeFull();
	}

	public void setSaveHandler(SaveHandler saveHandler) {
		this.saveHandler = Optional.of(saveHandler);
	}

	private void updateViewport() {
		preview.getElement().getClassList().remove("mobile");
		preview.getElement().getClassList().remove("tablet");
		preview.getElement().getClassList().remove("desktop");

		if (viewportSize.getSelectedIndex() == 1) {
			preview.getElement().getClassList().add("mobile");
		}

		if (viewportSize.getSelectedIndex() == 2) {
			preview.getElement().getClassList().add("tablet");
		}

		if (viewportSize.getSelectedIndex() == 3) {
			preview.getElement().getClassList().add("desktop");
		}
	}

}
