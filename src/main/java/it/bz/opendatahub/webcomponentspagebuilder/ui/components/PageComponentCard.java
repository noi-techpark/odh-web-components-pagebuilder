package it.bz.opendatahub.webcomponentspagebuilder.ui.components;

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
 * the page.
 * 
 * @author danielrampanelli
 */
public class PageComponentCard extends Card {

	private static final long serialVersionUID = -814467441008035093L;

	private PageComponent component;

	public PageComponentCard(PageComponent component) {
		super(new TitleLabel(component.getTitle()).withWhiteSpaceNoWrap(),
				new SecondaryLabel(component.getDescription()));

		this.component = component;

		addClassName("page-component-card");
		setWidth("240px");
		setHeight("180px");

		ObjectMapper objectMapper = new ObjectMapper();

		Map<String, Object> data = new HashMap<>();
		data.put("uid", component.getUid());
		data.put("tag", component.getTagName());
		data.put("assets", component.getAssets());
		data.put("markup", component.getDefaultMarkup() != null ? component.getDefaultMarkup()
				: String.format("<%s></%s>", component.getTagName(), component.getTagName()));

		getElement().getNode().runWhenAttached(ui -> {
			try {
				ui.getPage()
						.executeJavaScript("$0.addEventListener('dragstart', function(e) { "
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
