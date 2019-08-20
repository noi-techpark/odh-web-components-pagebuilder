package it.bz.opendatahub.webcomponentspagebuilder.data.repositories;

import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Page;

@Transactional
public interface PageRepository extends JpaRepository<Page, UUID> {

	@Query("SELECT p FROM Page p WHERE p.archived = false")
	List<Page> findAllActive();

}