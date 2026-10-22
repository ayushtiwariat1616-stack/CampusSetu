package edu.kiet.campussetu.incident;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SharedIncidentRepository extends JpaRepository<SharedIncident, Long> {
    Optional<SharedIncident> findByIncidentNumber(String incidentNumber);
    Page<SharedIncident> findByStatusNotOrderByCreatedAtDesc(SharedIncidentStatus status, Pageable pageable);
    List<SharedIncident> findByResponsibleQueueCodeIn(Collection<String> queueCodes);
    List<SharedIncident> findTop20ByOrderByCreatedAtDesc();
}
