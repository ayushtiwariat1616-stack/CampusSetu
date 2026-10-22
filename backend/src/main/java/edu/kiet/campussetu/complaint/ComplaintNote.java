package edu.kiet.campussetu.complaint;

import edu.kiet.campussetu.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "complaint_notes", indexes = {
        @Index(name = "idx_note_complaint", columnList = "complaint_id")
})
public class ComplaintNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_id", nullable = false)
    private Long complaintId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 3000)
    private String content;

    @Column(nullable = false)
    private boolean internalOnly; // TRUE = operator internal note (hidden from student); FALSE = student visible reply

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ComplaintNote() {}

    public ComplaintNote(Long complaintId, User author, String content, boolean internalOnly) {
        this.complaintId = complaintId;
        this.author = author;
        this.content = content;
        this.internalOnly = internalOnly;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isInternalOnly() { return internalOnly; }
    public void setInternalOnly(boolean internalOnly) { this.internalOnly = internalOnly; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
