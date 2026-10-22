package edu.kiet.campussetu.auth;

import edu.kiet.campussetu.audit.AuditService;
import edu.kiet.campussetu.common.InvalidOperationException;
import edu.kiet.campussetu.common.ResourceNotFoundException;
import edu.kiet.campussetu.user.OperatorKey;
import edu.kiet.campussetu.user.OperatorKeyRepository;
import edu.kiet.campussetu.user.User;
import edu.kiet.campussetu.user.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class OperatorKeyService {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int KEY_LENGTH = 24;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);
    private static final Duration DEFAULT_VALIDITY = Duration.ofDays(90);

    private final OperatorKeyRepository operatorKeyRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    public OperatorKeyService(OperatorKeyRepository operatorKeyRepository,
                              UserRepository userRepository,
                              AuditService auditService) {
        this.operatorKeyRepository = operatorKeyRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    /**
     * Generates a new cryptographically random operator key for an approved operator.
     * The plaintext key is returned ONCE to the authorized administrator and never stored.
     */
    @Transactional
    public GeneratedKeyResult generateNewKey(Long operatorUserId, String adminEmail) {
        User user = userRepository.findById(operatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + operatorUserId));

        // Revoke any existing key
        Optional<OperatorKey> existingKey = operatorKeyRepository.findByUserId(operatorUserId);
        existingKey.ifPresent(k -> {
            k.setRevoked(true);
            operatorKeyRepository.save(k);
        });

        // Generate raw random key: format OPK-XXXX-XXXX-XXXX
        StringBuilder sb = new StringBuilder("OPK-");
        for (int i = 0; i < KEY_LENGTH; i++) {
            if (i > 0 && i % 4 == 0) sb.append("-");
            sb.append(CHARS.charAt(secureRandom.nextInt(CHARS.length())));
        }
        String rawKey = sb.toString();
        String keyHash = passwordEncoder.encode(rawKey);
        String keyPrefix = rawKey.substring(0, 8); // e.g. "OPK-7A9B"

        Instant expiresAt = Instant.now().plus(DEFAULT_VALIDITY);
        OperatorKey keyEntity = new OperatorKey(user, keyHash, keyPrefix, expiresAt);
        operatorKeyRepository.save(keyEntity);

        auditService.record("OPERATOR_KEY", adminEmail, "KEY_GENERATED", "User:" + user.getEmail(),
                "Generated key with prefix " + keyPrefix + " expiring " + expiresAt, "INTERNAL");

        return new GeneratedKeyResult(rawKey, keyPrefix, expiresAt);
    }

    /**
     * Validates an operator key during login. Throttles attempts and locks on repeated failures.
     */
    @Transactional
    public boolean verifyKey(Long operatorUserId, String rawKeyInput, String clientIp) {
        if (rawKeyInput == null || rawKeyInput.trim().isEmpty()) {
            return false;
        }

        OperatorKey key = operatorKeyRepository.findByUserId(operatorUserId)
                .orElse(null);

        if (key == null) {
            auditService.record("OPERATOR_KEY", "UNKNOWN", "KEY_VERIFY_FAILED_NO_KEY",
                    "UserId:" + operatorUserId, "No operator key configured", clientIp);
            return false;
        }

        String userEmail = key.getUser().getEmail();

        if (key.isRevoked()) {
            auditService.record("OPERATOR_KEY", userEmail, "KEY_VERIFY_REVOKED",
                    "KeyPrefix:" + key.getKeyPrefix(), "Attempted to use revoked key", clientIp);
            throw new InvalidOperationException("This operator key has been revoked. Contact the administrator.");
        }

        if (key.isExpired()) {
            auditService.record("OPERATOR_KEY", userEmail, "KEY_VERIFY_EXPIRED",
                    "KeyPrefix:" + key.getKeyPrefix(), "Attempted to use expired key", clientIp);
            throw new InvalidOperationException("This operator key has expired. Request a new key from the administrator.");
        }

        if (key.isLocked()) {
            auditService.record("OPERATOR_KEY", userEmail, "KEY_VERIFY_LOCKED",
                    "KeyPrefix:" + key.getKeyPrefix(), "Account locked until " + key.getLockedUntil(), clientIp);
            throw new InvalidOperationException("Operator access temporarily locked due to multiple failed attempts. Try again after 15 minutes.");
        }

        boolean matches = passwordEncoder.matches(rawKeyInput.trim(), key.getKeyHash());
        if (matches) {
            key.setFailedAttempts(0);
            key.setLockedUntil(null);
            key.setLastUsedAt(Instant.now());
            operatorKeyRepository.save(key);

            auditService.record("OPERATOR_KEY", userEmail, "KEY_VERIFY_SUCCESS",
                    "KeyPrefix:" + key.getKeyPrefix(), "Operator key verified successfully", clientIp);
            return true;
        } else {
            int attempts = key.getFailedAttempts() + 1;
            key.setFailedAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                key.setLockedUntil(Instant.now().plus(LOCKOUT_DURATION));
                auditService.record("OPERATOR_KEY", userEmail, "KEY_LOCKED",
                        "KeyPrefix:" + key.getKeyPrefix(), "Maximum failed attempts reached. Locked for 15 minutes", clientIp);
            } else {
                auditService.record("OPERATOR_KEY", userEmail, "KEY_VERIFY_FAILED",
                        "KeyPrefix:" + key.getKeyPrefix(), "Failed attempt " + attempts + " of " + MAX_FAILED_ATTEMPTS, clientIp);
            }
            operatorKeyRepository.save(key);
            return false;
        }
    }

    public record GeneratedKeyResult(String rawKey, String prefix, Instant expiresAt) {}
}
