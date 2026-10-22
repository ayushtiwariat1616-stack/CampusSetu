package edu.kiet.campussetu.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperatorAssignmentRepository extends JpaRepository<OperatorAssignment, Long> {
    List<OperatorAssignment> findByUserIdAndActiveTrue(Long userId);
    List<OperatorAssignment> findByScopeTargetAndActiveTrue(String scopeTarget);
    List<OperatorAssignment> findByUserId(Long userId);
}
