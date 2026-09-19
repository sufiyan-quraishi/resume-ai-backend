package com.fortunecloud.resumeai.model;


public class Education {
    private String degree;        // e.g. "MCA", "B.Tech in Computer Science"
    private String institution;   // e.g. "IICMR College, Pune"
    private String year;          // e.g. "2020 - 2022"
    private String score;         // e.g. "8.4 CGPA" or "First Class"
	public String getDegree() {
		return degree;
	}
	public void setDegree(String degree) {
		this.degree = degree;
	}
	public String getInstitution() {
		return institution;
	}
	public void setInstitution(String institution) {
		this.institution = institution;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
    
}
