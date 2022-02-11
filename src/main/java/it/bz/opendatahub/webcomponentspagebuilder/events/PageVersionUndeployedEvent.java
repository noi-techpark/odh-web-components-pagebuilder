package it.bz.opendatahub.webcomponentspagebuilder.events;

import java.io.Serializable;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;

/**
 * Event object denoting that a page version has been undeployed (removed from
 * the online storage/layer).
 * 
 * @author danielrampanelli
 */
public class PageVersionUndeployedEvent implements Serializable {

	private static final long serialVersionUID = 5429508058519505132L;

	private String versionId;

	public PageVersionUndeployedEvent(PageVersion pageVersion) {
		this.versionId = pageVersion.getIdAsString();
	}

	public String getVersionId() {
		return versionId;
	}

}
