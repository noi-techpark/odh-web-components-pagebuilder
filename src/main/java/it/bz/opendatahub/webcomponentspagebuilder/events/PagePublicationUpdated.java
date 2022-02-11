package it.bz.opendatahub.webcomponentspagebuilder.events;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublication;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublicationAction;
import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublicationStatus;

public class PagePublicationUpdated implements Serializable {

	private static final long serialVersionUID = -4193763624152057378L;

	private UUID entityID;

	private PagePublicationAction action;

	private PagePublicationStatus status;

	private LocalDateTime datetime;

	public PagePublicationUpdated(PagePublication publication) {
		this.entityID = publication.getId();
		this.action = publication.getAction();
		this.status = publication.getStatus();
		this.datetime = publication.getUpdatedAt();
	}

	public UUID getEntityID() {
		return entityID;
	}

	public PagePublicationAction getAction() {
		return action;
	}

	public PagePublicationStatus getStatus() {
		return status;
	}

	public LocalDateTime getDatetime() {
		return datetime;
	}

	public boolean isAbout(PagePublication publication) {
		return publication.getId().equals(entityID);
	}

	public boolean isAbout(UUID id) {
		return id.equals(entityID);
	}

}
