package edu.kiet.campussetu.academic;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_document_pages", indexes = {
        @Index(name = "idx_doc_page_doc", columnList = "document_id")
})
public class AcademicDocumentPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(nullable = false)
    private int pageNumber;

    @Column(columnDefinition = "TEXT", length = 30000)
    private String pageContent;

    public AcademicDocumentPage() {}

    public AcademicDocumentPage(Long documentId, int pageNumber, String pageContent) {
        this.documentId = documentId;
        this.pageNumber = pageNumber;
        this.pageContent = pageContent;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    public String getPageContent() { return pageContent; }
    public void setPageContent(String pageContent) { this.pageContent = pageContent; }
}
