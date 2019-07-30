package it.bz.opendatahub.webcomponentspagebuilder.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Placeholder component shown to the user when there are no contents on the page
 */
public class PageContentsAreaPlaceholder extends VerticalLayout {

	private static final long serialVersionUID = 3438986252076416754L;

	public PageContentsAreaPlaceholder() {
		Icon icon = new Icon(VaadinIcon.AREA_SELECT);
		icon.addClassName("icon");

		Div text = new Div();
		text.addClassName("message");
		text.add("Add components to this page by dragging the available ones from the sidebar!");

		add(icon);
		add(text);
		addClassName("page-contents-area-placeholder");

		setHeight(null);
	}

}
