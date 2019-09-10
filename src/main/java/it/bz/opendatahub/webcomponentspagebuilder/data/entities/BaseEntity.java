package it.bz.opendatahub.webcomponentspagebuilder.data.entities;

import java.util.UUID;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Abstract base class for all entities, defines actually just a non-progressive
 * identifier property (UUID v4).
 * 
 * @author danielrampanelli
 */
@MappedSuperclass
public abstract class BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID id;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getIdAsString() {
		return getId().toString();
	}

}
