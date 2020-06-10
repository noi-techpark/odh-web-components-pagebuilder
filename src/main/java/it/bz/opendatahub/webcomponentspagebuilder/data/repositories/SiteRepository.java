package it.bz.opendatahub.webcomponentspagebuilder.data.repositories;

import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.Site;

/**
 * Repository component for accessing sites.
 * 
 * @author danielrampanelli
 */
@Transactional
public interface SiteRepository extends JpaRepository<Site, UUID> {

}