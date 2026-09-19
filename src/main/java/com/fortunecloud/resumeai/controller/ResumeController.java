package com.fortunecloud.resumeai.controller;

import com.fortunecloud.resumeai.dto.AnalysisResult;
import com.fortunecloud.resumeai.dto.CoverLetterRequest;
import com.fortunecloud.resumeai.dto.ExportRequest;
import com.fortunecloud.resumeai.dto.GeneratedContent;
import com.fortunecloud.resumeai.dto.ResumeRequest;
import com.fortunecloud.resumeai.service.AiClient;
import com.fortunecloud.resumeai.service.AtsAnalysisService;
import com.fortunecloud.resumeai.service.ExportService;
import com.fortunecloud.resumeai.service.ResumeService;
import com.fortunecloud.resumeai.service.ResumeTextExtractor;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ResumeController {

    private final ResumeService resumeService;
    private final AtsAnalysisService atsAnalysisService;
    private final ResumeTextExtractor textExtractor;
    private final ExportService exportService;
    private final AiClient aiClient;
    
    public ResumeController(ResumeService resumeService,
							AtsAnalysisService atsAnalysisService,
							ResumeTextExtractor textExtractor,
							ExportService exportService,
							AiClient aiClient) {
		this.resumeService = resumeService;
		this.atsAnalysisService = atsAnalysisService;
		this.textExtractor = textExtractor;
		this.exportService = exportService;
		this.aiClient = aiClient;
	}
    
    

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "aiConfigured", aiClient.isConfigured());
    }

    @PostMapping("/resume/generate")
    public GeneratedContent generateResume(@Valid @RequestBody ResumeRequest request) {
        return resumeService.generateResume(request);
    }

    @PostMapping("/cover-letter/generate")
    public GeneratedContent generateCoverLetter(@Valid @RequestBody CoverLetterRequest request) {
        return resumeService.generateCoverLetter(request);
    }

    /** Analyse an uploaded resume (PDF/TXT) against an optional job description. */
    @PostMapping(value = "/resume/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalysisResult analyze(@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "jobDescription", required = false) String jobDescription) {
        String text = textExtractor.extract(file);
        return atsAnalysisService.analyze(text, jobDescription);
    }

    /** Analyse pasted resume text instead of an upload. */
    @PostMapping("/resume/analyze-text")
    public AnalysisResult analyzeText(@RequestBody Map<String, String> body) {
        String resumeText = body.getOrDefault("resumeText", "");
        if (resumeText.isBlank()) {
            throw new IllegalArgumentException("resumeText must not be empty");
        }
        return atsAnalysisService.analyze(resumeText, body.get("jobDescription"));
    }

    @PostMapping("/export/markdown")
    public ResponseEntity<byte[]> exportMarkdown(@Valid @RequestBody ExportRequest request) {
        byte[] bytes = request.getContent().getBytes(StandardCharsets.UTF_8);
        return fileResponse(bytes, request.getFileName() + ".md", "text/markdown");
    }

    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@Valid @RequestBody ExportRequest request) {
        byte[] bytes = exportService.toPdf(request.getContent());
        return fileResponse(bytes, request.getFileName() + ".pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @PostMapping("/export/docx")
    public ResponseEntity<byte[]> exportDocx(@Valid @RequestBody ExportRequest request) throws IOException {
        byte[] bytes = exportService.toDocx(request.getContent());
        return fileResponse(bytes, request.getFileName() + ".docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    private ResponseEntity<byte[]> fileResponse(byte[] bytes, String filename, String contentType) {
        ContentDisposition disposition = ContentDisposition.attachment().filename(filename).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);
    }
}
