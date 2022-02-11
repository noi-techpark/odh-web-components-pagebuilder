package it.bz.opendatahub.webcomponentspagebuilder.data.repositories;

import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublication;

/**
 * Repository component for accessing page publications.
 * 
 * @author danielrampanelli
 */
@Transactional
public interface PagePublicationRepository extends JpaRepository<PagePublication, UUID> {

	@Query("SELECT p FROM PagePublication p WHERE p.status IN ('PENDING', 'PROGRESSING') ORDER BY p.updatedAt ASC")
	List<PagePublication> findAllActive();

	@Query("SELECT p FROM PagePublication p WHERE p.status IN ('COMPLETED') ORDER BY p.updatedAt DESC")
	List<PagePublication> findAllCompleted();

}