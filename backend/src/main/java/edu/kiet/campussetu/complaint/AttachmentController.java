package edu.kiet.campussetu.complaint;

import edu.kiet.campussetu.auth.AuthHelper;
import edu.kiet.campussetu.auth.SessionUser;
import edu.kiet.campussetu.common.ApiResponse;
import edu.kiet.campussetu.common.InvalidOperationException;
import edu.kiet.campussetu.common.ResourceNotFoundException;
import edu.kiet.campussetu.user.User;
import edu.kiet.campussetu.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "application/pdf"
    );

    private final AttachmentRepository attachmentRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintService complaintService;
    private final UserRepository userRepository;
    private final AuthHelper authHelper;

    @Value("${campussetu.attachment.upload-dir:./storage/attachments}")
    private String uploadDir;

    public AttachmentController(AttachmentRepository attachmentRepository,
                                ComplaintRepository complaintRepository,
                                ComplaintService complaintService,
                                UserRepository userRepository,
                                AuthHelper authHelper) {
        this.attachmentRepository = attachmentRepository;
        this.complaintRepository = complaintRepository;
        this.complaintService = complaintService;
        this.userRepository = userRepository;
        this.authHelper = authHelper;
    }

    /**
     * Upload private evidence file attached to a complaint.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Attachment>> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("complaintId") Long complaintId,
            HttpServletRequest request) throws IOException {

        SessionUser user = authHelper.requireApprovedUser(request);

        if (file.isEmpty()) {
            throw new InvalidOperationException("Uploaded file is empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidOperationException("Unsupported file type. Only JPEG, PNG, WEBP images and PDF documents are allowed.");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + complaintId));

        // Access check
        boolean isOwner = user.isStudent() && complaint.getStudent().getId().equals(user.getUserId());
        List<String> queues = complaintService.getAuthorizedQueueCodesForOperator(user);
        boolean isOp = user.isOperator() && queues.contains(complaint.getCurrentQueueCode());

        if (!isOwner && !isOp && !user.isSystemAdmin()) {
            throw new AccessDeniedException("You are not authorized to upload evidence to this complaint.");
        }

        Path storageDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(storageDir);

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "evidence.bin";
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : ".bin";
        String safeName = "EVID-" + UUID.randomUUID() + extension;
        Path targetPath = storageDir.resolve(safeName);

        // Security check: path traversal prevention
        if (!targetPath.startsWith(storageDir)) {
            throw new InvalidOperationException("Invalid target file path.");
        }

        Files.copy(file.getInputStream(), targetPath);

        User uploader = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Attachment attachment = new Attachment(
                complaintId,
                originalName,
                targetPath.toString(),
                contentType,
                file.getSize(),
                uploader
        );

        Attachment saved = attachmentRepository.save(attachment);
        return ResponseEntity.ok(ApiResponse.ok("Attachment uploaded successfully.", saved));
    }

    /**
     * Authorized download endpoint with strict privacy check.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long id,
            HttpServletRequest request) throws IOException {

        SessionUser user = authHelper.requireApprovedUser(request);
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + id));

        Complaint complaint = complaintRepository.findById(attachment.getComplaintId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated complaint not found"));

        boolean isOwner = user.isStudent() && complaint.getStudent().getId().equals(user.getUserId());
        List<String> queues = complaintService.getAuthorizedQueueCodesForOperator(user);
        boolean isOp = user.isOperator() && queues.contains(complaint.getCurrentQueueCode());

        if (!isOwner && !isOp && !user.isSystemAdmin()) {
            throw new AccessDeniedException("Access denied: You do not have permission to view this private evidence file.");
        }

        Path filePath = Paths.get(attachment.getStoragePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new ResourceNotFoundException("Attachment file not accessible on storage volume.");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getOriginalFileName() + "\"")
                .body(resource);
    }
}
