package it.bz.opendatahub.webcomponentspagebuilder.data.repositories;

import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.bz.opendatahub.webcomponentspagebuilder.data.entities.PageVersion;

/**
 * Repository component for accessing page versions.
 * 
 * @author danielrampanelli
 */
@Transactional
public interface PageVersionRepository extends JpaRepository<PageVersion, UUID> {

	@Query("SELECT v FROM PageVersion v WHERE v.hash = :hash")
	Optional<PageVersion> findByHash(@Param("hash") String hash);

}