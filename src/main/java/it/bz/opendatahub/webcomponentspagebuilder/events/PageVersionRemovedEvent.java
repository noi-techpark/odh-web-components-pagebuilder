package it.bz.opendatahub.webcomponentspagebuilder.events;

import java.io.Serializable;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;

/**
 * Event object denoting that a page version has been removed - namely discarded
 * or unpublished.
 * 
 * @author danielrampanelli
 */
public class PageVersionRemovedEvent implements Serializable {

	private static final long serialVersionUID = -6583619696210053035L;

	private String versionId;

	public PageVersionRemovedEvent(PageVersion pageVersion) {
		this.versionId = pageVersion.getIdAsString();
	}

	public String getVersionId() {
		return versionId;
	}

}
