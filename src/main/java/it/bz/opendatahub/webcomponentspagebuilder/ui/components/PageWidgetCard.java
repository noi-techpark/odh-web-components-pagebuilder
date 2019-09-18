package it.bz.opendatahub.webcomponentspagebuilder.ui.components;

import com.github.appreciated.card.Card;
import com.github.appreciated.card.action.Actions;
import com.github.appreciated.card.content.Item;

import it.bz.opendatahub.webcomponentspagebuilder.data.PageComponent;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageWidget;

/**
 * Represents a web component placed on a page as a widget, allows editing and
 * removing directly since the element is not present on the page in a
 * predictable way.
 * 
 * @author danielrampanelli
 */
public class PageWidgetCard extends Card {

	private static final long serialVersionUID = 1400612243685471723L;

	public interface WidgetActionsHandler {

		public void editWidget(PageWidget widget);

		public void removeWidget(PageWidget widget);

	}

	private PageComponent component;

	private PageWidget widget;

	public PageWidgetCard(PageComponent component, PageWidget widget, WidgetActionsHandler actionsHandler) {
		super(new Item(component.getTitle(), component.getDescription()),
				new Actions(
						new ThemableActionButton("Edit", e -> actionsHandler.editWidget(widget))
								.withTheme("tertiary small"),
						new ThemableActionButton("Remove", e -> actionsHandler.removeWidget(widget))
								.withTheme("tertiary small error")));

		this.component = component;
		this.widget = widget;

		addClassName("page-widget-card");
		setWidth("360px");
	}

	public PageComponent getComponent() {
		return component;
	}

	public PageWidget getWidget() {
		return widget;
	}

}
