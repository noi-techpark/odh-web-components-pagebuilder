package it.bz.opendatahub.webcomponentspagebuilder.events;

import java.io.Serializable;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;

/**
 * Event object denoting that a page version has been changed and updated.
 * 
 * @author danielrampanelli
 */
public class PageVersionUpdatedEvent implements Serializable {

	private static final long serialVersionUID = -7247820196930396874L;

	private String versionId;

	public PageVersionUpdatedEvent(PageVersion pageVersion) {
		this.versionId = pageVersion.getIdAsString();
	}

	public String getVersionId() {
		return versionId;
	}

}
