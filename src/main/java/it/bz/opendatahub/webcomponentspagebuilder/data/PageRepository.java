package it.bz.opendatahub.webcomponentspagebuilder.data;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface PageRepository extends JpaRepository<Page, UUID> {

	@Query("SELECT p FROM Page p WHERE p.hash = :hash")
	Optional<Page> findByHash(@Param("hash") String hash);

	@Query("SELECT p FROM Page p WHERE p.archived = false")
	List<Page> findAllActive();

}