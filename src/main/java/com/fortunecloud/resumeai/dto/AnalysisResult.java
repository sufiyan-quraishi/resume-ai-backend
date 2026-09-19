package com.fortunecloud.resumeai.dto;

import java.util.ArrayList;
import java.util.List;

public class AnalysisResult {

	/** 0-100 overall ATS readiness score. */
	private int overallScore;

	/** 0-100 keyword match against the job description (0 if no JD given). */
	private int keywordMatchScore;

	private boolean jobDescriptionProvided;

	public int getOverallScore() {
		return overallScore;
	}
	public void setOverallScore(int overallScore) {
		this.overallScore = overallScore;
	}
	public int getKeywordMatchScore() {
		return keywordMatchScore;
	}
	public void setKeywordMatchScore(int keywordMatchScore) {
		this.keywordMatchScore = keywordMatchScore;
	}
	public boolean isJobDescriptionProvided() {
		return jobDescriptionProvided;
	}
	public void setJobDescriptionProvided(boolean jobDescriptionProvided) {
		this.jobDescriptionProvided = jobDescriptionProvided;
	}
	public int getWordCount() {
		return wordCount;
	}
	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}
	public List<String> getMatchedKeywords() {
		return matchedKeywords;
	}
	public void setMatchedKeywords(List<String> matchedKeywords) {
		this.matchedKeywords = matchedKeywords;
	}
	public List<String> getMissingKeywords() {
		return missingKeywords;
	}
	public void setMissingKeywords(List<String> missingKeywords) {
		this.missingKeywords = missingKeywords;
	}
	public List<AnalysisCheck> getChecks() {
		return checks;
	}
	public void setChecks(List<AnalysisCheck> checks) {
		this.checks = checks;
	}
	public List<String> getSuggestions() {
		return suggestions;
	}
	public void setSuggestions(List<String> suggestions) {
		this.suggestions = suggestions;
	}
	private int wordCount;

	private List<String> matchedKeywords = new ArrayList<>();
	private List<String> missingKeywords = new ArrayList<>();
	private List<AnalysisCheck> checks = new ArrayList<>();
	private List<String> suggestions = new ArrayList<>();
}
