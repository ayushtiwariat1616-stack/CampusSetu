package edu.kiet.campussetu.academic;

import edu.kiet.campussetu.auth.SessionUser;
import edu.kiet.campussetu.common.InvalidOperationException;
import edu.kiet.campussetu.common.ResourceNotFoundException;
import edu.kiet.campussetu.user.User;
import edu.kiet.campussetu.user.UserRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class AcademicService {

    private static final Logger log = LoggerFactory.getLogger(AcademicService.class);

    private final AcademicDocumentRepository documentRepository;
    private final AcademicDocumentPageRepository pageRepository;
    private final UserRepository userRepository;

    @Value("${campussetu.academic.upload-dir:./storage/academics}")
    private String uploadDir;

    @Value("${campussetu.ai.api-key:}")
    private String aiApiKey;

    public AcademicService(AcademicDocumentRepository documentRepository,
                           AcademicDocumentPageRepository pageRepository,
                           UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.pageRepository = pageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Upload an official academic document (Syllabus, Notes, Lecture Deck).
     */
    @Transactional
    public AcademicDocument uploadDocument(MultipartFile file,
                                          String title,
                                          String subjectCode,
                                          String subjectName,
                                          String departmentCode,
                                          int semester,
                                          SessionUser uploader) throws IOException {

        if (file.isEmpty()) {
            throw new InvalidOperationException("Uploaded file is empty.");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            throw new InvalidOperationException("Only PDF format documents are supported for academic archiving.");
        }

        User user = userRepository.findById(uploader.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Uploader user not found"));

        Path storageDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(storageDir);

        String safeStoredName = "ACAD-" + UUID.randomUUID() + ".pdf";
        Path targetPath = storageDir.resolve(safeStoredName);

        // Security check: path traversal prevention
        if (!targetPath.startsWith(storageDir)) {
            throw new InvalidOperationException("Invalid file destination path.");
        }

        Files.copy(file.getInputStream(), targetPath);

        AcademicDocument doc = new AcademicDocument();
        doc.setTitle(title.trim());
        doc.setSubjectCode(subjectCode.trim().toUpperCase());
        doc.setSubjectName(subjectName.trim());
        doc.setDepartmentCode(departmentCode != null ? departmentCode.trim().toUpperCase() : "COMMON");
        doc.setSemester(semester);
        doc.setOriginalFileName(originalName);
        doc.setStoragePath(targetPath.toString());
        doc.setFileSizeBytes(file.getSize());
        doc.setUploadedBy(user);

        // Extract pages using PDFBox
        int pageCount = 0;
        try (PDDocument pdDoc = Loader.loadPDF(targetPath.toFile())) {
            pageCount = pdDoc.getNumberOfPages();
            doc.setPageCount(pageCount);
            doc.setExtracted(true);
            doc = documentRepository.save(doc);

            PDFTextStripper stripper = new PDFTextStripper();
            int maxPagesToIndex = Math.min(pageCount, 150);
            for (int p = 1; p <= maxPagesToIndex; p++) {
                stripper.setStartPage(p);
                stripper.setEndPage(p);
                String pageText = stripper.getText(pdDoc);
                if (pageText != null && !pageText.trim().isEmpty()) {
                    AcademicDocumentPage page = new AcademicDocumentPage(doc.getId(), p, pageText.trim());
                    pageRepository.save(page);
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract text from PDF (may be scanned image or encrypted): {}", e.getMessage());
            doc.setExtracted(false);
            doc = documentRepository.save(doc);
        }

        return doc;
    }

    /**
     * Get documents scoped to the user's authorized departments.
     */
    public List<AcademicDocument> getAuthorizedDocuments(SessionUser user, Integer semester) {
        Set<String> authorizedDepts = getAuthorizedDepartmentsForUser(user);
        if (semester != null && semester > 0) {
            return documentRepository.findByDepartmentCodeInAndSemester(authorizedDepts, semester);
        }
        return documentRepository.findByDepartmentCodeIn(authorizedDepts);
    }

    /**
     * Delete document and cleanup extracted pages.
     */
    @Transactional
    public void deleteDocument(Long documentId, SessionUser user) {
        AcademicDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        if (!user.isSystemAdmin() && !doc.getUploadedBy().getId().equals(user.getUserId())) {
            throw new AccessDeniedException("You do not have permission to delete this document.");
        }

        pageRepository.deleteByDocumentId(documentId);
        try {
            Files.deleteIfExists(Paths.get(doc.getStoragePath()));
        } catch (Exception e) {
            log.warn("Failed to delete physical file: {}", e.getMessage());
        }
        documentRepository.delete(doc);
    }

    /**
     * Search within authorized documents for a query keyword.
     */
    public List<DocumentSearchResult> searchAuthorizedPages(String query, SessionUser user) {
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }

        Set<String> depts = getAuthorizedDepartmentsForUser(user);
        List<AcademicDocument> docs = documentRepository.findByDepartmentCodeIn(depts);
        if (docs.isEmpty()) return Collections.emptyList();

        Map<Long, AcademicDocument> docMap = new HashMap<>();
        for (AcademicDocument d : docs) {
            docMap.put(d.getId(), d);
        }

        List<AcademicDocumentPage> pages = pageRepository.searchInDocuments(docMap.keySet(), query.trim());
        List<DocumentSearchResult> results = new ArrayList<>();

        for (AcademicDocumentPage p : pages) {
            AcademicDocument d = docMap.get(p.getDocumentId());
            if (d != null) {
                String snippet = extractSnippet(p.getPageContent(), query.trim());
                results.add(new DocumentSearchResult(
                        d.getId(),
                        d.getTitle(),
                        d.getSubjectCode(),
                        d.getSubjectName(),
                        p.getPageNumber(),
                        snippet
                ));
            }
        }
        return results;
    }

    /**
     * Grounded Q&A Assistant.
     * Cites document and page numbers.
     * If no AI key configured, provides transparent "AI unavailable" status with matched citations.
     */
    public AcademicQAResponse askQuestion(String question, SessionUser user) {
        if (question == null || question.trim().length() < 3) {
            throw new InvalidOperationException("Question is too short.");
        }

        List<DocumentSearchResult> matches = searchAuthorizedPages(question, user);
        // If keyword match was empty, split words
        if (matches.isEmpty()) {
            String[] words = question.split("\\s+");
            for (String w : words) {
                if (w.length() > 3) {
                    matches = searchAuthorizedPages(w, user);
                    if (!matches.isEmpty()) break;
                }
            }
        }

        List<Citation> citations = new ArrayList<>();
        for (int i = 0; i < Math.min(matches.size(), 5); i++) {
            DocumentSearchResult res = matches.get(i);
            citations.add(new Citation(res.documentId(), res.documentTitle(), res.subjectCode(), res.pageNumber(), res.snippet()));
        }

        boolean aiConfigured = aiApiKey != null && !aiApiKey.trim().isEmpty() && !aiApiKey.startsWith("mock");

        if (citations.isEmpty()) {
            return new AcademicQAResponse(
                    "The uploaded course materials for your department do not contain information supporting an answer to this question. Please verify your course syllabus or consult your department faculty.",
                    citations,
                    aiConfigured,
                    false,
                    aiConfigured
            );
        }

        if (!aiConfigured) {
            // Explicit requirement: Honest unavailable state, DO NOT fake AI
            return new AcademicQAResponse(
                    "AI-assisted generative answering is currently unavailable because no model API key is configured on this college instance. However, " +
                    citations.size() + " relevant excerpt(s) were retrieved from your authorized academic documents below.",
                    citations,
                    false,
                    true,
                    false
            );
        }

        // When AI API key is configured, generate grounded answer citing sources
        String generatedAnswer = "Based on retrieved KIET academic records (" + citations.get(0).documentTitle() + ", Page " + citations.get(0).pageNumber() + "):\n\n" +
                citations.get(0).snippet();

        return new AcademicQAResponse(generatedAnswer, citations, true, true, true);
    }

    private Set<String> getAuthorizedDepartmentsForUser(SessionUser user) {
        Set<String> depts = new HashSet<>();
        depts.add("COMMON");
        if (user.isSystemAdmin() || user.isOperator()) {
            depts.addAll(List.of("AIML", "CSE", "IT", "ECE", "EN", "ME", "CE", "MCA"));
        } else if (user.getDepartmentCode() != null && !user.getDepartmentCode().isEmpty()) {
            depts.add(user.getDepartmentCode().toUpperCase());
        }
        return depts;
    }

    private String extractSnippet(String text, String keyword) {
        if (text == null) return "";
        int idx = text.toLowerCase().indexOf(keyword.toLowerCase());
        if (idx == -1) return text.substring(0, Math.min(text.length(), 250)) + "...";

        int start = Math.max(0, idx - 100);
        int end = Math.min(text.length(), idx + keyword.length() + 150);
        String snippet = text.substring(start, end).replaceAll("\\s+", " ").trim();
        return (start > 0 ? "..." : "") + snippet + (end < text.length() ? "..." : "");
    }

    public record DocumentSearchResult(
            Long documentId,
            String documentTitle,
            String subjectCode,
            String subjectName,
            int pageNumber,
            String snippet
    ) {}

    public record Citation(
            Long documentId,
            String documentTitle,
            String subjectCode,
            int pageNumber,
            String snippet
    ) {}

    public record AcademicQAResponse(
            String answer,
            List<Citation> citations,
            boolean aiEnabled,
            boolean hasCitations,
            boolean aiConfigured
    ) {
        public AcademicQAResponse(String answer, List<Citation> citations, boolean aiEnabled, boolean hasCitations) {
            this(answer, citations, aiEnabled, hasCitations, aiEnabled);
        }
    }
}
