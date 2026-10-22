package edu.kiet.campussetu.complaint;

import edu.kiet.campussetu.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "attachments", indexes = {
        @Index(name = "idx_attachment_complaint", columnList = "complaint_id")
})
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_id")
    private Long complaintId;

    @Column(nullable = false, length = 250)
    private String originalFileName;

    @Column(nullable = false, length = 350)
    private String storagePath;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private long fileSizeBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Attachment() {}

    public Attachment(Long complaintId, String originalFileName, String storagePath, String contentType, long fileSizeBytes, User uploadedBy) {
        this.complaintId = complaintId;
        this.originalFileName = originalFileName;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.uploadedBy = uploadedBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
