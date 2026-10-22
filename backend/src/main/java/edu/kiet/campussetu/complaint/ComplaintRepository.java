package edu.kiet.campussetu.complaint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByTrackingNumber(String trackingNumber);

    Page<Complaint> findByStudentIdOrderByCreatedAtDesc(Long studentId, Pageable pageable);

    Page<Complaint> findByCurrentQueueCodeInOrderByCreatedAtDesc(Collection<String> queueCodes, Pageable pageable);

    List<Complaint> findBySharedIncidentId(Long sharedIncidentId);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.currentQueueCode IN :queueCodes AND c.status = :status")
    long countByQueueCodesAndStatus(@Param("queueCodes") Collection<String> queueCodes, @Param("status") ComplaintStatus status);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status = :status")
    long countByStatus(@Param("status") ComplaintStatus status);

    List<Complaint> findByStatusIn(Collection<ComplaintStatus> statuses);

    List<Complaint> findTop100ByOrderByCreatedAtDesc();
}
