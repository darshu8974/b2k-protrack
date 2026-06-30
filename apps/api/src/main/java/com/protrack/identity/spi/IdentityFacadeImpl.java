package com.protrack.identity.spi;

import com.protrack.identity.domain.User;
import com.protrack.identity.repository.UserRepository;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link IdentityFacade} backed by {@link UserRepository}. */
@Service
public class IdentityFacadeImpl implements IdentityFacade {

	private final UserRepository userRepository;

	public IdentityFacadeImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public boolean existsById(UUID userId) {
		return userRepository.existsById(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public Set<UUID> findExistingIds(Collection<UUID> userIds) {
		return userRepository.findAllById(userIds).stream()
				.map(User::getId)
				.collect(Collectors.toSet());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserBrief> findBrief(UUID userId) {
		return userRepository.findById(userId).map(IdentityFacadeImpl::toBrief);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, UserBrief> findBriefs(Collection<UUID> userIds) {
		return userRepository.findAllById(userIds).stream()
				.collect(Collectors.toMap(User::getId, IdentityFacadeImpl::toBrief));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UUID> findOrganizationId(UUID userId) {
		return userRepository.findById(userId).map(User::getOrganizationId);
	}

	private static UserBrief toBrief(User user) {
		return new UserBrief(user.getId(), user.getFullName(), user.getEmail(),
				user.getAvatarInitials(), user.getAvatarColor());
	}
}
