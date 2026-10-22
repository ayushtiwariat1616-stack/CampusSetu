package edu.kiet.campussetu.academic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AcademicDocumentPageRepository extends JpaRepository<AcademicDocumentPage, Long> {

    List<AcademicDocumentPage> findByDocumentIdOrderByPageNumberAsc(Long documentId);

    void deleteByDocumentId(Long documentId);

    @Query("SELECT p FROM AcademicDocumentPage p WHERE p.documentId IN :docIds AND LOWER(CAST(p.pageContent AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<AcademicDocumentPage> searchInDocuments(@Param("docIds") Collection<Long> docIds, @Param("keyword") String keyword);
    List<AcademicDocumentPage> findByDocumentIdIn(Collection<Long> docIds);
}
