package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

@Transactional
public interface PageContentRepository extends JpaRepository<PageContent, UUID> {

}