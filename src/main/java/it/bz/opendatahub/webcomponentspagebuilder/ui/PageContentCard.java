package it.bz.opendatahub.webcomponentspagebuilder.ui;

import org.vaadin.stefan.dnd.drag.DragSourceExtension;

import com.github.appreciated.card.Card;
import com.github.appreciated.card.label.SecondaryLabel;
import com.github.appreciated.card.label.TitleLabel;

import it.bz.opendatahub.webcomponentspagebuilder.data.PageComponent;

/**
 * Present the available web components and support dragging the component onto the page
 */
public class PageContentCard extends Card {

	private static final long serialVersionUID = -814467441008035093L;

	private PageComponent component;

	public PageContentCard(PageComponent component) {
		super(new TitleLabel(component.getTitle()).withWhiteSpaceNoWrap(),
				new SecondaryLabel(component.getDescription()));

		this.component = component;

		addClassName("page-content-card");
		setWidth("240px");
		setHeight("180px");

		DragSourceExtension.extend(this);
	}

	public PageComponent getComponent() {
		return component;
	}

}
