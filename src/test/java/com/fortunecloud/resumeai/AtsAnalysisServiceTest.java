package com.fortunecloud.resumeai;

import com.fortunecloud.resumeai.dto.AnalysisResult;
import com.fortunecloud.resumeai.service.AtsAnalysisService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AtsAnalysisServiceTest {

    private final AtsAnalysisService service = new AtsAnalysisService();

    @Test
    void matchesKeywordsFromJobDescription() {
        String resume = """
                # Vaibhav Barde
                vaibhav@example.com | +91 90000 00000
                ## Summary
                Java developer who built and led delivery of Spring Boot microservices.
                ## Skills
                Java, Spring Boot, React, MySQL, REST API
                ## Experience
                ### Senior Developer - Fortune Cloud
                - Designed REST APIs and reduced latency by 30% for 2000 users
                ## Education
                MCA, IICMR College
                """;
        String jd = "Looking for a Java developer with Spring Boot, React and REST API experience.";

        AnalysisResult result = service.analyze(resume, jd);

        assertThat(result.isJobDescriptionProvided()).isTrue();
        assertThat(result.getMatchedKeywords()).contains("java", "react");
        assertThat(result.getKeywordMatchScore()).isGreaterThan(0);
        assertThat(result.getOverallScore()).isBetween(0, 100);
    }

    @Test
    void flagsMissingContactAndMetrics() {
        String resume = "Worked on some projects. Responsible for various tasks.";
        AnalysisResult result = service.analyze(resume, null);

        assertThat(result.isJobDescriptionProvided()).isFalse();
        assertThat(result.getChecks()).anyMatch(c -> c.getName().equals("Contact information") && !c.isPassed());
        assertThat(result.getSuggestions()).isNotEmpty();
    }
}
