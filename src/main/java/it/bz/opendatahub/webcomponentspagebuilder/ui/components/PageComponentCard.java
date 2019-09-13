package it.bz.opendatahub.webcomponentspagebuilder.ui.components;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.appreciated.card.Card;
import com.github.appreciated.card.action.Actions;
import com.github.appreciated.card.content.Item;

import it.bz.opendatahub.webcomponentspagebuilder.data.PageComponent;

/**
 * Present the available web components and support dragging the component onto
 * the page.
 * 
 * @author danielrampanelli
 */
public class PageComponentCard extends Card {

	private static final long serialVersionUID = -814467441008035093L;

	public interface ComponentActionsHandler {

		public void addComponentToContents(PageComponent component);

		public void addComponentToWidgets(PageComponent component);

	}

	private PageComponent component;

	public PageComponentCard(PageComponent component, ComponentActionsHandler actionsHandler) {
		super(new Item(component.getTitle(), component.getDescription()), new Actions(
				new ThemableActionButton("Add To Contents", e -> actionsHandler.addComponentToContents(component))
						.withTheme("tertiary small"),
				new ThemableActionButton("Add To Widgets", e -> actionsHandler.addComponentToWidgets(component))
						.withTheme("tertiary small")));

		this.component = component;

		addClassName("page-component-card");
		setWidth("360px");

		ObjectMapper objectMapper = new ObjectMapper();

		Map<String, Object> data = new HashMap<>();
		data.put("uid", component.getUid());
		data.put("tag", component.getTagName());
		data.put("assets", component.getAssets());
		data.put("markup", component.getMarkup());

		getElement().getNode().runWhenAttached(ui -> {
			try {
				ui.getPage().executeJavaScript("$0.addEventListener('dragstart', function(e) { "
						+ "e.dataTransfer.setData('componentToInsert', JSON.stringify("
						+ objectMapper.writeValueAsString(data) + "));" + "});", PageComponentCard.this);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		getElement().setProperty("draggable", true);
	}

	public PageComponent getComponent() {
		return component;
	}

}
