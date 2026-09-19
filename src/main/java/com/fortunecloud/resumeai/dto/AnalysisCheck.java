package com.fortunecloud.resumeai.dto;


public class AnalysisCheck {
    private String name;       // e.g. "Contact information"
    private boolean passed;
    private String severity;   // "high" | "medium" | "low"
    private String message;    // what was found / how to fix
    
    public AnalysisCheck(String name, boolean passed, String severity, String message) {
  		this.name = name;
  		this.passed = passed;
  		this.severity = severity;
  		this.message = message;
  	}
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isPassed() {
		return passed;
	}
	public void setPassed(boolean passed) {
		this.passed = passed;
	}
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
    
  
}
