package com.fortunecloud.resumeai.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Pulls plain text out of an uploaded resume so it can be analysed.
 * Supports PDF (via PDFBox) and plain text / markdown.
 */
@Service
public class ResumeTextExtractor {

    public String extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a non-empty PDF or TXT resume");
        }

        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();

        try {
            if (name.endsWith(".pdf") || contentType.contains("pdf")) {
                return extractPdf(file);
            }
            if (name.endsWith(".txt") || name.endsWith(".md") || contentType.startsWith("text/")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the uploaded file: " + e.getMessage());
        }
        throw new IllegalArgumentException("Unsupported file type. Upload a PDF or TXT file.");
    }

    private String extractPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
