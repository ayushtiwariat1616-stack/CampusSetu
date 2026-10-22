package edu.kiet.campussetu.incident;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentSubscriptionRepository extends JpaRepository<IncidentSubscription, Long> {
    Optional<IncidentSubscription> findByIncidentIdAndStudentId(Long incidentId, Long studentId);
    boolean existsByIncidentIdAndStudentId(Long incidentId, Long studentId);
    int countByIncidentId(Long incidentId);
    List<IncidentSubscription> findByStudentId(Long studentId);
    void deleteByIncidentIdAndStudentId(Long incidentId, Long studentId);
}
