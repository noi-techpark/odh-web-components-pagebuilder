package it.bz.opendatahub.webcomponentspagebuilder.events;

import java.io.Serializable;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;

/**
 * Event object denoting that a page version has been deployed and uploaded to
 * the online storage.
 * 
 * @author danielrampanelli
 */
public class PageVersionDeployedEvent implements Serializable {

	private static final long serialVersionUID = -1588257936984345566L;

	private String versionId;

	public PageVersionDeployedEvent(PageVersion pageVersion) {
		this.versionId = pageVersion.getIdAsString();
	}

	public String getVersionId() {
		return versionId;
	}

}
