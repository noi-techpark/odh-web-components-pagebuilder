package it.bz.opendatahub.webcomponentspagebuilder.data.entities;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Entity representing a web component placed on a {@link PageVersion} as a
 * widget, outside of the page's content area.
 * 
 * @author danielrampanelli
 */
@Entity
@Table(name = "pagebuilder_page_widget")
public class PageWidget extends PageElement {

	@Column(name = "widget_id")
	@NotNull
	private UUID widgetID;

	public PageWidget() {

	}

	public UUID getWidgetID() {
		return widgetID;
	}

	public void setWidgetID(UUID widgetID) {
		this.widgetID = widgetID;
	}

	public PageWidget copy() {
		PageWidget copy = new PageWidget();
		copy.setUid(getUid());
		copy.setWidgetID(getWidgetID());
		copy.setTagName(getTagName());
		copy.setAssets(getAssets());
		copy.setMarkup(getMarkup());
		copy.setPosition(getPosition());

		return copy;
	}

}
