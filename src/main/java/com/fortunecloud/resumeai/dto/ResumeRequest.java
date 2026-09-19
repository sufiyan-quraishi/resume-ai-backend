package com.fortunecloud.resumeai.dto;

import com.fortunecloud.resumeai.model.Education;
import com.fortunecloud.resumeai.model.Experience;
import com.fortunecloud.resumeai.model.ProjectItem;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything the user supplies to build a resume.
 * Most fields are optional - the generator fills gaps sensibly.
 */
public class ResumeRequest {

    public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLinkedIn() {
		return linkedIn;
	}

	public void setLinkedIn(String linkedIn) {
		this.linkedIn = linkedIn;
	}

	public String getPortfolio() {
		return portfolio;
	}

	public void setPortfolio(String portfolio) {
		this.portfolio = portfolio;
	}

	public String getGithub() {
		return github;
	}

	public void setGithub(String github) {
		this.github = github;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getTargetRole() {
		return targetRole;
	}

	public void setTargetRole(String targetRole) {
		this.targetRole = targetRole;
	}

	public String getJobDescription() {
		return jobDescription;
	}

	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
	}

	public List<Education> getEducation() {
		return education;
	}

	public void setEducation(List<Education> education) {
		this.education = education;
	}

	public List<Experience> getExperience() {
		return experience;
	}

	public void setExperience(List<Experience> experience) {
		this.experience = experience;
	}

	public List<ProjectItem> getProjects() {
		return projects;
	}

	public void setProjects(List<ProjectItem> projects) {
		this.projects = projects;
	}

	public List<String> getSkills() {
		return skills;
	}

	public void setSkills(List<String> skills) {
		this.skills = skills;
	}

	public List<String> getCertifications() {
		return certifications;
	}

	public void setCertifications(List<String> certifications) {
		this.certifications = certifications;
	}

	public List<String> getAchievements() {
		return achievements;
	}

	public void setAchievements(List<String> achievements) {
		this.achievements = achievements;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	@NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "A valid email is required")
    private String email;

    private String phone;
    private String location;
    private String linkedIn;
    private String portfolio;
    private String github;

    /** Optional professional summary. If blank, one is generated. */
    private String summary;

    /** The role the resume is being tailored for, e.g. "Senior Java Full Stack Developer". */
    private String targetRole;

    /** Optional job description to tailor the resume against. */
    private String jobDescription;

    private List<Education> education = new ArrayList<>();
    private List<Experience> experience = new ArrayList<>();
    private List<ProjectItem> projects = new ArrayList<>();
    private List<String> skills = new ArrayList<>();
    private List<String> certifications = new ArrayList<>();
    private List<String> achievements = new ArrayList<>();

    /** Output language, e.g. "English", "Marathi", "Japanese". Defaults to English. */
    private String language = "English";

    /** Visual style hint: "modern" | "classic" | "minimal". */
    private String template = "modern";
}
