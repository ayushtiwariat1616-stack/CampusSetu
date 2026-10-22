package edu.kiet.campussetu.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {
    List<Department> findByActiveTrue();
    Optional<Department> findByCodeIgnoreCase(String code);
}
