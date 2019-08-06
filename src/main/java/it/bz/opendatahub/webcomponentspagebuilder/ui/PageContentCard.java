package it.bz.opendatahub.webcomponentspagebuilder.ui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.appreciated.card.Card;
import com.github.appreciated.card.label.SecondaryLabel;
import com.github.appreciated.card.label.TitleLabel;

import it.bz.opendatahub.webcomponentspagebuilder.data.PageComponent;

/**
 * Present the available web components and support dragging the component onto
 * the page
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

		ObjectMapper objectMapper = new ObjectMapper();

		Map<String, Object> data = new HashMap<>();
		data.put("assets", Arrays.asList(component.getAssetUrl()));
		data.put("tag", component.getTag());
		data.put("markup", component.getDefaultMarkup());

		getElement().getNode().runWhenAttached(ui -> {
			try {
				ui.getPage()
						.executeJavaScript("$0.addEventListener('dragstart', function(e) { "
								+ "e.dataTransfer.setData('componentToInsert', JSON.stringify("
								+ objectMapper.writeValueAsString(data) + "));" + "});", PageContentCard.this);
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
