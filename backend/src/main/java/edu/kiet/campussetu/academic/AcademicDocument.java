package edu.kiet.campussetu.academic;

import edu.kiet.campussetu.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "academic_documents", indexes = {
        @Index(name = "idx_academic_dept", columnList = "departmentCode"),
        @Index(name = "idx_academic_sem", columnList = "semester"),
        @Index(name = "idx_academic_subject", columnList = "subjectCode")
})
public class AcademicDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 30)
    private String subjectCode; // e.g. "KAI-601"

    @Column(nullable = false, length = 150)
    private String subjectName; // e.g. "Artificial Intelligence & Expert Systems"

    @Column(nullable = false, length = 50)
    private String departmentCode; // "AIML", "CSE", "COMMON"

    @Column(nullable = false)
    private int semester; // 1 to 8

    @Column(nullable = false, length = 250)
    private String originalFileName;

    @Column(nullable = false, length = 350)
    private String storagePath;

    @Column(nullable = false)
    private long fileSizeBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    @Column(nullable = false)
    private int pageCount = 0;

    @Column(nullable = false)
    private boolean extracted = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AcademicDocument() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }

    public boolean isExtracted() { return extracted; }
    public void setExtracted(boolean extracted) { this.extracted = extracted; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
