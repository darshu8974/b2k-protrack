package com.protrack.identity.repository;

import com.protrack.identity.domain.RefreshToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link RefreshToken}; lookups are by the stored SHA-256 hash. */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	List<RefreshToken> findByUserIdAndRevokedAtIsNull(UUID userId);
}
