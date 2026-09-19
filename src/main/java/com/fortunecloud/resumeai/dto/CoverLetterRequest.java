package com.fortunecloud.resumeai.dto;

import jakarta.validation.constraints.NotBlank;


import java.util.ArrayList;
import java.util.List;


public class CoverLetterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String email;
    private String phone;

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String hiringManager;     // optional, e.g. "Ms. your name"
    private String jobTitle;          // role you are applying for

    @NotBlank(message = "Job description is required to tailor the letter")
    private String jobDescription;

    private String yearsOfExperience; // e.g. "6"
    private List<String> highlights = new ArrayList<>();  // key wins to weave in
    private List<String> skills = new ArrayList<>();

    /** "professional" | "enthusiastic" | "formal" */
    private String tone = "professional";

    private String language = "English";

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

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getHiringManager() {
		return hiringManager;
	}

	public void setHiringManager(String hiringManager) {
		this.hiringManager = hiringManager;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getJobDescription() {
		return jobDescription;
	}

	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
	}

	public String getYearsOfExperience() {
		return yearsOfExperience;
	}

	public void setYearsOfExperience(String yearsOfExperience) {
		this.yearsOfExperience = yearsOfExperience;
	}

	public List<String> getHighlights() {
		return highlights;
	}

	public void setHighlights(List<String> highlights) {
		this.highlights = highlights;
	}

	public List<String> getSkills() {
		return skills;
	}

	public void setSkills(List<String> skills) {
		this.skills = skills;
	}

	public String getTone() {
		return tone;
	}

	public void setTone(String tone) {
		this.tone = tone;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
