package it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

@SuppressWarnings("serial")
public abstract class BaseDialog extends Dialog {

	private Div titleComponent;
	private VerticalLayout contents;

	public BaseDialog() {
		titleComponent = new Div();
		titleComponent.addClassName("dialog-title");

		Button fullscreenButton = new Button();
		fullscreenButton.addClassName("dialog-fullscreen-button");
		fullscreenButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
		fullscreenButton.setIcon(VaadinIcon.EXPAND_SQUARE.create());

		fullscreenButton.addClickListener(e -> {
			if (getElement().getAttribute("theme").equals("custom-base-dialog-fullscreen")) {
				getElement().setAttribute("theme", "custom-base-dialog");

				fullscreenButton.setIcon(VaadinIcon.EXPAND_SQUARE.create());

				configureSize();
			} else {
				getElement().setAttribute("theme", "custom-base-dialog-fullscreen");

				fullscreenButton.setIcon(VaadinIcon.COMPRESS_SQUARE.create());

				setWidth("100%");
				setHeight("100%");
			}
		});

		Button closeButton = new Button(VaadinIcon.CLOSE.create());
		closeButton.addClassName("dialog-close-button");
		closeButton.addClickListener(e -> close());
		closeButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);

		HorizontalLayout headerButtons = new HorizontalLayout();
		headerButtons.setSpacing(false);
		headerButtons.add(fullscreenButton);
		headerButtons.add(closeButton);

		HorizontalLayout headerBar = new HorizontalLayout();
		headerBar.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		headerBar.setWidthFull();
		headerBar.add(titleComponent);
		headerBar.expand(titleComponent);
		headerBar.add(headerButtons);

		contents = new VerticalLayout();
		contents.addClassName("dialog-contents");
		contents.setSizeFull();

		VerticalLayout contentsWrapper = new VerticalLayout();
		contentsWrapper.addClassName("dialog-contents-wrapper");
		contentsWrapper.setMargin(false);
		contentsWrapper.setPadding(true);
		contentsWrapper.setSizeFull();
		contentsWrapper.add(contents);

		VerticalLayout layout = new VerticalLayout();
		layout.addClassName("custom-base-dialog");
		layout.setMargin(false);
		layout.setPadding(false);
		layout.setSpacing(false);
		layout.setSizeFull();
		layout.add(headerBar);
		layout.add(contentsWrapper);

		add(layout);
		getElement().setAttribute("theme", "custom-base-dialog");

		configureSize();
	}

	protected void configureSize() {
		// noop
	}

	protected void setDialogTitle(String title) {
		titleComponent.setText(title);
	}

	protected VerticalLayout getDialogContents() {
		return contents;
	}

}
