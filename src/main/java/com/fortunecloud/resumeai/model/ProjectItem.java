package com.fortunecloud.resumeai.model;


public class ProjectItem {
    private String name;          // e.g. "TrainerDesk Attendance App"
    private String techStack;     // e.g. "Spring Boot, MySQL, Vanilla JS"
    private String description;    // what it does / your role
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTechStack() {
		return techStack;
	}
	public void setTechStack(String techStack) {
		this.techStack = techStack;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
    
}
