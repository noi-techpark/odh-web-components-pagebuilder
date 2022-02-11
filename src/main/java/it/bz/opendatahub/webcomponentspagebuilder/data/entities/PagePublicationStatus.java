package it.bz.opendatahub.webcomponentspagebuilder.data.entities;

/**
 * List of possible status that can be associated to a page publication entity.
 * 
 * @author danielrampanelli
 */
public enum PagePublicationStatus {

	PENDING("Pending"),

	PROGRESSING("In Progress"),

	COMPLETED("Completed");

	private String label;

	private PagePublicationStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
