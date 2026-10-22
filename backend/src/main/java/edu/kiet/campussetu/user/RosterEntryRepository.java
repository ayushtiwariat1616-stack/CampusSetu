package edu.kiet.campussetu.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RosterEntryRepository extends JpaRepository<RosterEntry, Long> {
    Optional<RosterEntry> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
