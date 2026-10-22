package edu.kiet.campussetu.complaint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintNoteRepository extends JpaRepository<ComplaintNote, Long> {
    List<ComplaintNote> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);
    List<ComplaintNote> findByComplaintIdAndInternalOnlyFalseOrderByCreatedAtAsc(Long complaintId);
}
