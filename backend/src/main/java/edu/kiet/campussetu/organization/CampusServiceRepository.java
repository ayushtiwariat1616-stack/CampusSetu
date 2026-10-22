package edu.kiet.campussetu.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampusServiceRepository extends JpaRepository<CampusService, String> {
    List<CampusService> findByActiveTrue();
    Optional<CampusService> findByCodeIgnoreCase(String code);
}
