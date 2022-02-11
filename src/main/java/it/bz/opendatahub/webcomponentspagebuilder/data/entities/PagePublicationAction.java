package it.bz.opendatahub.webcomponentspagebuilder.data.entities;

public enum PagePublicationAction {

	PUBLISH("Publish"),

	UNPUBLISH("Unpublish");

	private String label;

	private PagePublicationAction(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

}
