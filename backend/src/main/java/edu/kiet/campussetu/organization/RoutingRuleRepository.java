package edu.kiet.campussetu.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutingRuleRepository extends JpaRepository<RoutingRule, Long> {
    List<RoutingRule> findByActiveTrueOrderByPriorityOrderAsc();
    List<RoutingRule> findByCategoryAndActiveTrueOrderByPriorityOrderAsc(String category);
}
