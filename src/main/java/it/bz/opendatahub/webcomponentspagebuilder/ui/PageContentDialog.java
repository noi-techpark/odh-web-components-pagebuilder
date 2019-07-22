package it.bz.opendatahub.webcomponentspagebuilder.ui;

import java.util.Optional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;

/**
 * Dialog for managing the markup of a selected page content, which allows editing or reverting to the default markup
 */
public class PageContentDialog extends Dialog {

	private static final long serialVersionUID = -131927632298787518L;

	@FunctionalInterface
	public interface SaveHandler {
		public void save(String markup);
	}

	private Optional<SaveHandler> saveHandler = Optional.empty();

	public PageContentDialog(String markup, String defaultMarkup) {
		TextArea textarea = new TextArea();
		textarea.setValue(markup);
		textarea.setWidthFull();
		textarea.setHeight("240px");

		Button resetButton = new Button("RESTORE DEFAULT");
		resetButton.addClickListener(clickEvent -> {
			textarea.setValue(defaultMarkup);
		});

		Button saveButton = new Button("SAVE");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(clickEvent -> {
			saveHandler.ifPresent(handler -> handler.save(textarea.getValue()));
			close();
		});

		add(textarea);
		add(new HorizontalLayout(resetButton, saveButton));
		setWidth("480px");
	}

	public void setSaveHandler(SaveHandler saveHandler) {
		this.saveHandler = Optional.of(saveHandler);
	}

}
