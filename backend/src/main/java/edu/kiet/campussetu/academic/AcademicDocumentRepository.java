package edu.kiet.campussetu.academic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AcademicDocumentRepository extends JpaRepository<AcademicDocument, Long> {
    List<AcademicDocument> findByDepartmentCodeIn(Collection<String> departmentCodes);
    List<AcademicDocument> findByDepartmentCodeInAndSemester(Collection<String> departmentCodes, int semester);
    List<AcademicDocument> findTop20ByOrderByCreatedAtDesc();

    @Query("SELECT d FROM AcademicDocument d WHERE d.uploadedBy.id = :userId OR d.departmentCode IN :deptCodes ORDER BY d.createdAt DESC")
    List<AcademicDocument> findUserAccessibleDocuments(@Param("userId") Long userId, @Param("deptCodes") Collection<String> deptCodes);

    @Query("SELECT d FROM AcademicDocument d WHERE (d.uploadedBy.id = :userId OR d.departmentCode IN :deptCodes) AND d.semester = :semester ORDER BY d.createdAt DESC")
    List<AcademicDocument> findUserAccessibleDocumentsBySemester(@Param("userId") Long userId, @Param("deptCodes") Collection<String> deptCodes, @Param("semester") int semester);
}
