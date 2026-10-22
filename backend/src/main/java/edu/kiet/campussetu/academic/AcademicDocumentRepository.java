package edu.kiet.campussetu.academic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AcademicDocumentRepository extends JpaRepository<AcademicDocument, Long> {
    List<AcademicDocument> findByDepartmentCodeIn(Collection<String> departmentCodes);
    List<AcademicDocument> findByDepartmentCodeInAndSemester(Collection<String> departmentCodes, int semester);
    List<AcademicDocument> findTop20ByOrderByCreatedAtDesc();
}
