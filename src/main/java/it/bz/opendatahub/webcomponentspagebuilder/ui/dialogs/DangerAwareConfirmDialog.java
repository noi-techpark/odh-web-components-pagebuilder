package it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs;

import java.util.Optional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class DangerAwareConfirmDialog extends Dialog {

	private static final long serialVersionUID = 8617844422979368430L;

	@FunctionalInterface
	public interface ConfirmHandler {
		public void confirmed();
	}

	private H3 title;

	private Div message;

	private Button confirmButton;

	private Optional<ConfirmHandler> confirmHandler = Optional.empty();

	public DangerAwareConfirmDialog() {
		title = new H3();
		title.getStyle().set("margin", "1em 0 0 0");

		message = new Div();

		Button cancelButton = new Button();
		cancelButton.addClickListener(e -> close());
		cancelButton.setText("CANCEL");

		confirmButton = new Button();
		confirmButton.addClickListener(e -> onConfirmation());
		confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

		HorizontalLayout actions = new HorizontalLayout();
		actions.setMargin(false);
		actions.setPadding(false);
		actions.setWidthFull();

		actions.add(cancelButton);

		Div spacer = new Div();
		actions.add(spacer);
		actions.expand(spacer);

		actions.add(confirmButton);

		VerticalLayout content = new VerticalLayout();
		content.setMargin(false);
		content.setPadding(false);
		content.setSpacing(true);
		content.setWidthFull();

		content.add(title);
		content.add(message);
		content.add(actions);

		add(content);

		setWidth("480px");
	}

	public DangerAwareConfirmDialog withTitle(String title) {
		this.title.setText(title);
		return this;
	}

	public DangerAwareConfirmDialog withMessage(String message) {
		this.message.setText(message);
		return this;
	}

	public DangerAwareConfirmDialog withAction(String action) {
		this.confirmButton.setText(action);
		return this;
	}

	public DangerAwareConfirmDialog withHandler(ConfirmHandler handler) {
		this.confirmHandler = Optional.of(handler);
		return this;
	}

	public void onConfirmation() {
		this.confirmHandler.ifPresent(handler -> handler.confirmed());

		close();
	}

	public static DangerAwareConfirmDialog create() {
		return new DangerAwareConfirmDialog();
	}

}