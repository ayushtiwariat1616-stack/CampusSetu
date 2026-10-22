package edu.kiet.campussetu.audit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public void record(String category, String actorEmail, String action, String targetResource, String details, String ipAddress) {
        AuditEvent event = new AuditEvent(category, actorEmail, action, targetResource, details, ipAddress);
        auditEventRepository.save(event);
    }
}
