package it.bz.opendatahub.webcomponentspagebuilder.data.entities;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Entity representing a web component placed on a {@link PageVersion} at a
 * defined position of the content.
 * 
 * @author danielrampanelli
 */
@Entity
@Table(name = "pagebuilder_page_content")
public class PageContent extends PageElement {

	@Column(name = "content_id")
	@NotNull
	private UUID contentID;

	public PageContent() {

	}

	public UUID getContentID() {
		return contentID;
	}

	public void setContentID(UUID contentID) {
		this.contentID = contentID;
	}

	public PageContent copy() {
		PageContent copy = new PageContent();
		copy.setUid(getUid());
		copy.setContentID(getContentID());
		copy.setTagName(getTagName());
		copy.setAssets(getAssets());
		copy.setMarkup(getMarkup());
		copy.setPosition(getPosition());

		return copy;
	}

}
