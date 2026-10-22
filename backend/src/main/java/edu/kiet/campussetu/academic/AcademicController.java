package edu.kiet.campussetu.academic;

import edu.kiet.campussetu.auth.AuthHelper;
import edu.kiet.campussetu.auth.SessionUser;
import edu.kiet.campussetu.common.ApiResponse;
import edu.kiet.campussetu.common.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/academics")
public class AcademicController {

    private final AcademicService academicService;
    private final AcademicDocumentRepository documentRepository;
    private final AuthHelper authHelper;

    public AcademicController(AcademicService academicService,
                              AcademicDocumentRepository documentRepository,
                              AuthHelper authHelper) {
        this.academicService = academicService;
        this.documentRepository = documentRepository;
        this.authHelper = authHelper;
    }

    /**
     * List authorized course documents for student/operator.
     */
    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<List<AcademicDocument>>> getDocuments(
            @RequestParam(required = false) Integer semester,
            HttpServletRequest request) {

        SessionUser user = authHelper.requireApprovedUser(request);
        List<AcademicDocument> documents = academicService.getAuthorizedDocuments(user, semester);
        return ResponseEntity.ok(ApiResponse.ok(documents));
    }

    /**
     * Upload an academic document (PDF).
     */
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AcademicDocument>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("subjectCode") String subjectCode,
            @RequestParam("subjectName") String subjectName,
            @RequestParam(value = "departmentCode", defaultValue = "COMMON") String departmentCode,
            @RequestParam(value = "semester", defaultValue = "1") int semester,
            HttpServletRequest request) throws IOException {

        SessionUser user = authHelper.requireApprovedUser(request);
        AcademicDocument doc = academicService.uploadDocument(
                file, title, subjectCode, subjectName, departmentCode, semester, user);
        return ResponseEntity.ok(ApiResponse.ok("Academic document uploaded and indexed.", doc));
    }

    /**
     * Delete an academic document.
     */
    @DeleteMapping("/documents/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long id,
            HttpServletRequest request) {

        SessionUser user = authHelper.requireApprovedUser(request);
        academicService.deleteDocument(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Document removed successfully.", null));
    }

    /**
     * Keyword search across authorized course documents.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<AcademicService.DocumentSearchResult>>> search(
            @RequestParam("q") String query,
            HttpServletRequest request) {

        SessionUser user = authHelper.requireApprovedUser(request);
        List<AcademicService.DocumentSearchResult> results = academicService.searchAuthorizedPages(query, user);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    /**
     * Grounded academic document Q&A assistant with page citations.
     */
    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<AcademicService.AcademicQAResponse>> askQuestion(
            @RequestBody AskQuestionRequest req,
            HttpServletRequest request) {

        SessionUser user = authHelper.requireApprovedUser(request);
        AcademicService.AcademicQAResponse answer = academicService.askQuestion(req.question(), user);
        return ResponseEntity.ok(ApiResponse.ok(answer));
    }

    /**
     * Download or view document PDF.
     */
    @GetMapping("/documents/{id}/file")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            HttpServletRequest request) throws IOException {

        SessionUser user = authHelper.requireApprovedUser(request);
        AcademicDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

        Path filePath = Paths.get(doc.getStoragePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new ResourceNotFoundException("Physical file not accessible.");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getOriginalFileName() + "\"")
                .body(resource);
    }

    public record AskQuestionRequest(String question) {}
}
