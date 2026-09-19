package com.fortunecloud.resumeai.dto;

public class GeneratedContent {

    /** "RESUME" or "COVER_LETTER". */
    private String type;

    /** Markdown the front-end renders and lets the user edit. */
    private String markdown;

    /** True when produced by the LLM, false when produced by the offline template. */
    private boolean aiGenerated;

    // No-argument constructor
    public GeneratedContent() {
    }

    // All-argument constructor
    public GeneratedContent(String type, String markdown, boolean aiGenerated) {
        this.type = type;
        this.markdown = markdown;
        this.aiGenerated = aiGenerated;
    }

    // Getters and Setters

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        this.aiGenerated = aiGenerated;
    }
}