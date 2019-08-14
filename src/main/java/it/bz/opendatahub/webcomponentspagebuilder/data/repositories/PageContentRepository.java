package it.bz.opendatahub.webcomponentspagebuilder.data.repositories;

import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageContent;

@Transactional
public interface PageContentRepository extends JpaRepository<PageContent, UUID> {

}