package it.bz.opendatahub.webcomponentspagebuilder.ui.dialogs;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.spring.annotation.SpringComponent;

@Scope("prototype")
@SpringComponent
public class PageVersionCommentDialog extends Dialog {

	private static final long serialVersionUID = -8800880460449557395L;

	@FunctionalInterface
	public interface ConfirmHandler {
		public void confirmed(String comment);
	}

	private Optional<ConfirmHandler> confirmHandler = Optional.empty();

	@PostConstruct
	private void postConstruct() {
		TextArea commentField = new TextArea();
		commentField.setLabel("COMMENT");
		commentField.setPlaceholder("Changes or remarks of the updated page version");
		commentField.setWidthFull();
		commentField.setAutofocus(true);

		Button saveButton = new Button("CONTINUE");
		saveButton.addClickListener(clickEvent -> {
			confirmHandler.ifPresent(handler -> {
				String comment = commentField.getValue();
				handler.confirmed(comment);
			});

			close();
		});

		HorizontalLayout buttons = new HorizontalLayout(saveButton);

		VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.add(commentField);
		layout.add(buttons);
		layout.setHorizontalComponentAlignment(Alignment.END, buttons);

		add(layout);
		setWidth("480px");
	}

	public void setConfirmHandler(ConfirmHandler handler) {
		this.confirmHandler = Optional.of(handler);
	}

}