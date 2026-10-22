package edu.kiet.campussetu.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperatorKeyRepository extends JpaRepository<OperatorKey, Long> {
    Optional<OperatorKey> findByUserId(Long userId);
    Optional<OperatorKey> findByUserIdAndRevokedFalse(Long userId);
}
