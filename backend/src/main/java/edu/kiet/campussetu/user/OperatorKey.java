package edu.kiet.campussetu.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "operator_keys", indexes = {
        @Index(name = "idx_op_key_user", columnList = "user_id")
})
public class OperatorKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String keyHash; // BCrypt hash of randomly generated operator key

    @Column(nullable = false, length = 20)
    private String keyPrefix; // e.g. "OPK-7A9B..." for display/identification

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(nullable = false)
    private int failedAttempts = 0;

    private Instant lockedUntil;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant lastUsedAt;

    public OperatorKey() {}

    public OperatorKey(User user, String keyHash, String keyPrefix, Instant expiresAt) {
        this.user = user;
        this.keyHash = keyHash;
        this.keyPrefix = keyPrefix;
        this.expiresAt = expiresAt;
        this.revoked = false;
        this.failedAttempts = 0;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }

    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public Instant getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Instant lockedUntil) { this.lockedUntil = lockedUntil; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }
}
