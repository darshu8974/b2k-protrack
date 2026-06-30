package com.protrack.project.repository;

import com.protrack.project.domain.Imprint;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link Imprint}. */
public interface ImprintRepository extends JpaRepository<Imprint, UUID> {
}
