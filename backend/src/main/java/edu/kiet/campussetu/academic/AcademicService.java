package edu.kiet.campussetu.academic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

@Service
public class AcademicService {

    private static final Logger log = LoggerFactory.getLogger(AcademicService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final AcademicDocumentRepository documentRepository;
    private final AcademicDocumentPageRepository pageRepository;
    private final UserRepository userRepository;

    @Value("${campussetu.academic.upload-dir:./storage/academics}")
    private String uploadDir;

    @Value("${campussetu.groq.api-key:${GROQ_API_KEY:}}")
    private String configuredGroqApiKey;

    public AcademicService(AcademicDocumentRepository documentRepository,
                           AcademicDocumentPageRepository pageRepository,
                           UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.pageRepository = pageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Upload an official or student-provided academic document (PDF).
     */
    @Transactional
    public AcademicDocument uploadDocument(MultipartFile file,
                                          String title,
                                          String subjectCode,
                                          String subjectName,
                                          String departmentCode,
                                          int semester,
                                          SessionUser uploader) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new InvalidOperationException("Uploaded file cannot be empty.");
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf";
        if (!originalName.toLowerCase().endsWith(".pdf")) {
            throw new InvalidOperationException("Only PDF documents are supported for curriculum indexing.");
        }

        Path targetDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);

        String storedFilename = UUID.randomUUID() + "_" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path targetPath = targetDir.resolve(storedFilename).normalize();

        if (!targetPath.startsWith(targetDir)) {
            throw new InvalidOperationException("Invalid file destination path.");
        }

        Files.copy(file.getInputStream(), targetPath);

        User user = userRepository.findById(uploader.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AcademicDocument doc = new AcademicDocument();
        doc.setTitle(title.trim());
        doc.setSubjectCode(subjectCode != null && !subjectCode.isBlank() ? subjectCode.trim().toUpperCase() : "COURSE");
        doc.setSubjectName(subjectName != null && !subjectName.isBlank() ? subjectName.trim() : title.trim());
        doc.setDepartmentCode(departmentCode != null && !departmentCode.isBlank() ? departmentCode.trim().toUpperCase() : (uploader.getDepartmentCode() != null ? uploader.getDepartmentCode() : "COMMON"));
        doc.setSemester(semester > 0 ? semester : 1);
        doc.setOriginalFileName(originalName);
        doc.setStoragePath(targetPath.toString());
        doc.setFileSizeBytes(file.getSize());
        doc.setUploadedBy(user);

        // Extract pages using PDFBox 3.x
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
     * Get documents scoped to the user's authorized departments or uploaded by user.
     */
    public List<AcademicDocument> getAuthorizedDocuments(SessionUser user, Integer semester) {
        Set<String> authorizedDepts = getAuthorizedDepartmentsForUser(user);
        if (semester != null && semester > 0) {
            return documentRepository.findUserAccessibleDocumentsBySemester(user.getUserId(), authorizedDepts, semester);
        }
        return documentRepository.findUserAccessibleDocuments(user.getUserId(), authorizedDepts);
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

        List<AcademicDocument> docs = getAuthorizedDocuments(user, null);
        if (docs.isEmpty()) return Collections.emptyList();

        Map<Long, AcademicDocument> docMap = new HashMap<>();
        for (AcademicDocument d : docs) {
            docMap.put(d.getId(), d);
        }

        List<AcademicDocumentPage> matchedPages = pageRepository.searchInDocuments(docMap.keySet(), query.trim());
        List<DocumentSearchResult> results = new ArrayList<>();
        for (AcademicDocumentPage p : matchedPages) {
            AcademicDocument d = docMap.get(p.getDocumentId());
            if (d != null) {
                String snippet = extractSnippet(p.getPageContent(), query.trim(), 280);
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
     * AI-Powered Academic Assistant (supports Groq Llama 3 with intelligent built-in fallback).
     */
    public AcademicQAResponse askQuestion(String question,
                                          String customGroqApiKey,
                                          Long documentId,
                                          String model,
                                          SessionUser user) {

        if (question == null || question.trim().length() < 2) {
            throw new InvalidOperationException("Question is too short.");
        }

        List<DocumentSearchResult> matches;
        String documentContext = "";
        String docTitleForPrompt = "";

        if (documentId != null && documentId > 0) {
            AcademicDocument doc = documentRepository.findById(documentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
            docTitleForPrompt = doc.getTitle();

            List<AcademicDocumentPage> pages = pageRepository.findByDocumentIdOrderByPageNumberAsc(documentId);
            matches = new ArrayList<>();

            // Find matching pages or gather first few pages for general queries
            String qLower = question.toLowerCase();
            String[] qWords = qLower.split("\\s+");

            for (AcademicDocumentPage p : pages) {
                String contentLower = p.getPageContent().toLowerCase();
                boolean matched = false;
                for (String w : qWords) {
                    if (w.length() > 3 && contentLower.contains(w)) {
                        matched = true;
                        break;
                    }
                }
                if (matched || pages.size() <= 4) {
                    matches.add(new DocumentSearchResult(
                            doc.getId(), doc.getTitle(), doc.getSubjectCode(), doc.getSubjectName(),
                            p.getPageNumber(), extractSnippet(p.getPageContent(), question, 300)
                    ));
                    if (documentContext.length() < 4000) {
                        documentContext += "\n--- [Page " + p.getPageNumber() + "] ---\n" + p.getPageContent() + "\n";
                    }
                }
            }

            if (matches.isEmpty() && !pages.isEmpty()) {
                // If no direct keyword hit, provide initial pages
                for (int i = 0; i < Math.min(pages.size(), 3); i++) {
                    AcademicDocumentPage p = pages.get(i);
                    matches.add(new DocumentSearchResult(
                            doc.getId(), doc.getTitle(), doc.getSubjectCode(), doc.getSubjectName(),
                            p.getPageNumber(), p.getPageContent().length() > 250 ? p.getPageContent().substring(0, 250) + "..." : p.getPageContent()
                    ));
                    documentContext += "\n--- [Page " + p.getPageNumber() + "] ---\n" + p.getPageContent() + "\n";
                }
            }
        } else {
            matches = searchAuthorizedPages(question, user);
            if (matches.isEmpty()) {
                String[] words = question.split("\\s+");
                for (String w : words) {
                    if (w.length() > 3) {
                        matches = searchAuthorizedPages(w, user);
                        if (!matches.isEmpty()) break;
                    }
                }
            }
            StringBuilder sb = new StringBuilder();
            for (DocumentSearchResult m : matches) {
                sb.append("\n[Document: ").append(m.documentTitle()).append(", Page ").append(m.pageNumber()).append("]\n")
                  .append(m.snippet()).append("\n");
            }
            documentContext = sb.toString();
        }

        List<Citation> citations = new ArrayList<>();
        for (int i = 0; i < Math.min(matches.size(), 5); i++) {
            DocumentSearchResult res = matches.get(i);
            citations.add(new Citation(res.documentId(), res.documentTitle(), res.subjectCode(), res.pageNumber(), res.snippet()));
        }

        // Determine active Groq API Key
        String activeKey = (customGroqApiKey != null && !customGroqApiKey.isBlank())
                ? customGroqApiKey.trim()
                : (configuredGroqApiKey != null && !configuredGroqApiKey.isBlank() ? configuredGroqApiKey.trim() : null);

        String activeModel = (model != null && !model.isBlank()) ? model.trim() : "llama-3.3-70b-versatile";

        if (activeKey != null && !activeKey.startsWith("mock")) {
            try {
                String aiAnswer = callGroqLlama(activeKey, activeModel, question, documentContext, docTitleForPrompt);
                return new AcademicQAResponse(
                        aiAnswer,
                        citations,
                        true,
                        !citations.isEmpty(),
                        true,
                        "Groq AI (" + activeModel + ")"
                );
            } catch (Exception ex) {
                log.warn("Groq API call encountered an issue: {}", ex.getMessage());
            }
        }

        // Built-in intelligent synthesis fallback
        String fallbackAnswer = generateSynthesizedAnswer(question, citations, docTitleForPrompt, activeKey != null);
        return new AcademicQAResponse(
                fallbackAnswer,
                citations,
                true,
                !citations.isEmpty(),
                activeKey != null,
                activeKey != null ? "CampusSetu Synthesizer (Groq Timeout Fallback)" : "CampusSetu Built-in AI Engine"
        );
    }

    /**
     * Call Groq AI API with Llama 3 model.
     */
    private String callGroqLlama(String apiKey, String modelName, String question, String context, String docTitle) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(12))
                .build();

        String systemPrompt = "You are KIET CampusSetu's AI Academic & Research Assistant powered by Groq Llama 3. " +
                "You assist students with engineering and academic materials, lecture notes, and syllabus inquiries at KIET Group of Institutions. " +
                "Ground your answer in the provided document context whenever possible, explain step-by-step with formulas or code where applicable, " +
                "and cite page numbers accurately.";

        String userPrompt = (context.isBlank())
                ? "Student Question:\n" + question
                : "Academic Reference Material (" + (docTitle.isBlank() ? "Authorized Curriculum" : docTitle) + "):\n" +
                  context + "\n\nStudent Question:\n" + question + "\n\nPlease explain thoroughly, highlight key concepts, and cite page numbers where relevant.";

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", modelName);
        payload.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        payload.put("temperature", 0.4);
        payload.put("max_tokens", 1400);

        String requestBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(25))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText();
            }
        }

        log.warn("Groq returned status {}: {}", response.statusCode(), response.body());
        throw new IOException("Groq API returned HTTP " + response.statusCode());
    }

    /**
     * Synthesizes an intelligent, structured response when external API key is absent.
     */
    private String generateSynthesizedAnswer(String question, List<Citation> citations, String docTitle, boolean keyAttempted) {
        if (citations.isEmpty()) {
            return "### 📚 Curriculum Assistant Notice\n\n" +
                    "AI-assisted generative answering is currently unavailable for this question as no matching excerpts were found in your currently cataloged course notes.\n\n" +
                    "- **Tip 1**: Try broader keywords (e.g., 'BFS', 'Search', 'Stack', 'Tree', 'Evaluation').\n" +
                    "- **Tip 2**: You can upload your own course PDF or assignment sheet directly using the **'Upload PDF'** button above!\n" +
                    "- **Tip 3**: Connect your free Groq API key in the top settings box to enable open-domain Llama 3 answers.";
        }

        Citation top = citations.get(0);
        StringBuilder sb = new StringBuilder();
        if (!keyAttempted) {
            sb.append("*(AI-assisted generative answering is currently unavailable as no Groq key was supplied; grounded curriculum excerpts are synthesized below.)*\n\n");
        }
        sb.append("### 🧠 AI Concept Summary\n\n");
        sb.append("Based on **").append(top.documentTitle()).append("** (**Page ").append(top.pageNumber()).append("**):\n\n");
        sb.append("> \"").append(top.snippet().trim()).append("\"\n\n");

        sb.append("#### 💡 Key Takeaway & Analysis:\n");
        sb.append("- **Subject**: ").append(top.subjectCode()).append("\n");
        sb.append("- **Primary Reference**: Page ").append(top.pageNumber()).append(" provides the core theoretical foundation for your query regarding *\"").append(question.trim()).append("\"*.\n");
        sb.append("- **Academic Context**: Review the highlighted definition and ensure you understand the necessary mathematical or algorithmic constraints before class evaluations.\n\n");

        if (citations.size() > 1) {
            sb.append("#### 📑 Related Citations:\n");
            for (int i = 1; i < citations.size(); i++) {
                Citation c = citations.get(i);
                sb.append("- **Page ").append(c.pageNumber()).append("** (").append(c.subjectCode()).append("): ").append(c.snippet().substring(0, Math.min(100, c.snippet().length()))).append("...\n");
            }
            sb.append("\n");
        }

        sb.append("*(⚡ Tip: You can paste your free Groq API key above to activate live **Llama 3.3 70B** for full conversational code generation and deep step-by-step problem derivations!)*");

        return sb.toString();
    }

    private String extractSnippet(String text, String keyword, int maxLength) {
        if (text == null) return "";
        int idx = text.toLowerCase().indexOf(keyword.toLowerCase());
        if (idx == -1) {
            return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
        }
        int start = Math.max(0, idx - 60);
        int end = Math.min(text.length(), idx + keyword.length() + 180);
        String prefix = start > 0 ? "..." : "";
        String suffix = end < text.length() ? "..." : "";
        return prefix + text.substring(start, end).trim() + suffix;
    }

    private Set<String> getAuthorizedDepartmentsForUser(SessionUser user) {
        Set<String> depts = new HashSet<>();
        depts.add("COMMON");
        if (user.getDepartmentCode() != null && !user.getDepartmentCode().isBlank()) {
            depts.add(user.getDepartmentCode().trim().toUpperCase());
        }
        if (user.isSystemAdmin()) {
            depts.addAll(List.of("AIML", "CSE", "IT", "ECE", "EN", "ME", "CE", "MCA"));
        }
        return depts;
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
            boolean aiConfigured,
            String provider
    ) {
        public AcademicQAResponse(String answer, List<Citation> citations, boolean aiEnabled, boolean hasCitations) {
            this(answer, citations, aiEnabled, hasCitations, aiEnabled, "CampusSetu AI");
        }
    }
}
