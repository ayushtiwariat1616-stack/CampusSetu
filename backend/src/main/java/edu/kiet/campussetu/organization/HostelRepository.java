package edu.kiet.campussetu.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HostelRepository extends JpaRepository<Hostel, String> {
    List<Hostel> findByActiveTrue();
    Optional<Hostel> findByCodeIgnoreCase(String code);
}
