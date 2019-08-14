package it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs;

import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageContent;

/**
 * Dialog for managing the markup of a selected page content, which allows
 * editing or reverting to the default markup
 */
public class PageContentConfigurationDialog extends Dialog {

	private static final long serialVersionUID = -131927632298787518L;

	@FunctionalInterface
	public interface SaveHandler {
		public void save(String markup);
	}

	public class PageContentToUpdate {

		private String markup;

		public PageContentToUpdate() {

		}

		public String getMarkup() {
			return markup;
		}

		public void setMarkup(String markup) {
			this.markup = markup;
		}

	}

	private class MarkupValidator extends AbstractValidator<String> {

		private static final long serialVersionUID = -1229452354800924198L;

		private String tagName;

		protected MarkupValidator(String tagName, String errorMessage) {
			super(errorMessage);

			this.tagName = tagName;
		}

		@Override
		public ValidationResult apply(String value, ValueContext context) {
			if (value == null || value.isEmpty()) {
				return toResult(value, false);
			}

			Document document = Jsoup.parseBodyFragment(value);

			if (document.select("body *").size() != 1) {
				return toResult(value, false);
			}

			return toResult(value, document.select("body " + tagName).size() == 1);
		}

	}

	private Optional<SaveHandler> saveHandler = Optional.empty();

	public PageContentConfigurationDialog(PageContent pageContent) {
		TextArea textarea = new TextArea();
		textarea.setValue(pageContent.getMarkup());
		textarea.setWidthFull();
		textarea.setHeight("320px");

		Binder<PageContentToUpdate> binder = new Binder<>(PageContentToUpdate.class);

		binder.forField(textarea).asRequired()
				.withValidator(new MarkupValidator(pageContent.getTagName(), String.format(
						"Invalid markup, please use only a single and valid <%s> element.", pageContent.getTagName())))
				.bind("markup");

		Button saveButton = new Button("SAVE");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(clickEvent -> {
			saveHandler.ifPresent(handler -> handler.save(textarea.getValue()));
			close();
		});

		VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.add(textarea);
		layout.add(saveButton);
		layout.setHorizontalComponentAlignment(Alignment.END, saveButton);

		add(layout);
		setWidth("480px");
	}

	public void setSaveHandler(SaveHandler saveHandler) {
		this.saveHandler = Optional.of(saveHandler);
	}

}
