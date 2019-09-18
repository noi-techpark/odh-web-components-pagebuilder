package it.bz.opendatahub.webcomponentspagebuilder.data.repositories;

import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageWidget;

/**
 * Repository component for accessing page widgets.
 * 
 * @author danielrampanelli
 */
@Transactional
public interface PageWidgetRepository extends JpaRepository<PageWidget, UUID> {

}