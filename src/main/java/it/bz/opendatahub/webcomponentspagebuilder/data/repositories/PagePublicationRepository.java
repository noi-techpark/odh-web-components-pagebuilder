package it.bz.opendatahub.webcomponentspagebuilder.data.repositories;

import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PagePublication;

@Transactional
public interface PagePublicationRepository extends JpaRepository<PagePublication, UUID> {

}