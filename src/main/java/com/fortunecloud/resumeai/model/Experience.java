package com.fortunecloud.resumeai.model;



import java.util.ArrayList;
import java.util.List;


public class Experience {
    private String jobTitle;       // e.g. "Senior Java Developer"
    private String company;        // e.g. "Fortune Cloud Technologies"
    private String duration;       // e.g. "Jun 2022 - Present"
    private String location;       // e.g. "Pune, India"

    /** Raw bullet points describing what the person did / achieved. */
    private List<String> responsibilities = new ArrayList<>();

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public List<String> getResponsibilities() {
		return responsibilities;
	}

	public void setResponsibilities(List<String> responsibilities) {
		this.responsibilities = responsibilities;
	}
    
}
