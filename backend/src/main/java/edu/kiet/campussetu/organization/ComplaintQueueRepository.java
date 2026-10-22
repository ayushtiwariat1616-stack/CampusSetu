package edu.kiet.campussetu.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintQueueRepository extends JpaRepository<ComplaintQueue, Long> {
    Optional<ComplaintQueue> findByQueueCode(String queueCode);
    List<ComplaintQueue> findByTargetCode(String targetCode);
    List<ComplaintQueue> findByQueueType(String queueType);
    List<ComplaintQueue> findByActiveTrue();
}
