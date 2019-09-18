package it.bz.opendatahub.webcomponentspagebuilder.ui.components;

import com.github.appreciated.card.action.ActionButton;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;

/**
 * Action button that can be placed on a {@link Card} and which can be styled
 * with different themes.
 * 
 * @author danielrampanelli
 */
public class ThemableActionButton extends ActionButton {

	private static final long serialVersionUID = 3145394088579658895L;

	public ThemableActionButton(String text, ComponentEventListener<ClickEvent<Button>> clickListener) {
		super(text, clickListener);
	}

	public ThemableActionButton withTheme(String theme) {
		getElement().setAttribute("theme", theme);
		return this;
	}

}
