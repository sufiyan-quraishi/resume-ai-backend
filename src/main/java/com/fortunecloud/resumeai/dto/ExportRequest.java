package com.fortunecloud.resumeai.dto;

import jakarta.validation.constraints.NotBlank;


public class ExportRequest {
    public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/** Markdown content to export (possibly edited by the user). */
    @NotBlank(message = "Content is required")
    private String content;

    /** Base file name without extension, e.g. "vaibhav-barde-resume". */
    private String fileName = "document";
}
