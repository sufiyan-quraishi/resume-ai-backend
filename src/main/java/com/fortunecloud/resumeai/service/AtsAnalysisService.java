package com.fortunecloud.resumeai.service;

import com.fortunecloud.resumeai.dto.AnalysisCheck;
import com.fortunecloud.resumeai.dto.AnalysisResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Lightweight ATS analysis service.
 *
 * This service checks:
 * 1. Keyword matching between resume and job description
 * 2. Contact information
 * 3. Resume length
 * 4. Quantified impact / metrics
 * 5. Action verbs
 * 6. Weak/filler phrases
 * 7. Standard resume sections
 *
 * This is a rule-based implementation and does not require AI.
 */
@Service
public class AtsAnalysisService {

    private static final Pattern EMAIL =
            Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.-]+");

    private static final Pattern PHONE =
            Pattern.compile("(\\+?\\d[\\d ()-]{7,}\\d)");

    private static final Pattern METRIC =
            Pattern.compile(
                    "(\\d+%|\\$\\d+|\\b\\d{2,}\\b|\\b\\d+\\s*(k|x|hrs|hours|users|projects|students)\\b)",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Pattern TOKEN =
            Pattern.compile("[a-zA-Z][a-zA-Z+#.]{1,}");

    private static final Set<String> STOPWORDS = Set.of(
            "the", "and", "for", "with", "you", "your", "will", "are",
            "our", "this", "that", "have", "has", "from", "all", "can",
            "who", "but", "not", "they", "their", "them", "able", "etc",
            "via", "per", "any", "may", "must", "should", "would", "could",
            "into", "out", "use", "used", "using", "work", "working", "team",
            "role", "job", "new", "well", "across", "within", "while",
            "such", "also", "more", "most", "other", "than", "then",
            "experience", "years", "year", "strong", "good", "great",
            "looking", "join", "ability", "responsibilities", "requirements",
            "what", "how", "why", "when", "where", "which"
    );

    private static final Set<String> ACTION_VERBS = Set.of(
            "built", "designed", "developed", "led", "implemented", "created",
            "delivered", "improved", "reduced", "increased", "launched",
            "automated", "architected", "optimized", "migrated", "deployed",
            "managed", "mentored", "shipped", "scaled", "engineered",
            "integrated", "refactored", "streamlined", "spearheaded", "drove"
    );

    private static final Set<String> WEAK_PHRASES = Set.of(
            "responsible for",
            "duties included",
            "worked on",
            "helped with",
            "team player",
            "hard worker",
            "detail oriented",
            "go-getter",
            "think outside the box"
    );

    private static final List<String> SKILL_PHRASES = List.of(
            "spring boot",
            "spring security",
            "rest api",
            "restful",
            "ci/cd",
            "unit testing",
            "machine learning",
            "data structures",
            "object oriented",
            "micro services",
            "microservices",
            "version control",
            "message queue",
            "design patterns",
            "test driven",
            "node.js",
            "next.js",
            "react.js"
    );

    public AnalysisResult analyze(String resumeText, String jobDescription) {

        AnalysisResult result = new AnalysisResult();

        String resume = resumeText == null ? "" : resumeText;
        String resumeLower = resume.toLowerCase();

        // Tokenize resume
        List<String> words = tokenize(resume);

        result.setWordCount(words.size());

        // Check whether Job Description is provided
        boolean hasJd = StringUtils.hasText(jobDescription);

        result.setJobDescriptionProvided(hasJd);

        // ---------------------------------------------------------
        // Keyword matching
        // ---------------------------------------------------------

        int keywordScore = 0;

        if (hasJd) {

            Set<String> jdKeywords = extractKeywords(jobDescription);

            Set<String> resumeTokens =
                    new LinkedHashSet<>(
                            words.stream()
                                    .map(String::toLowerCase)
                                    .toList()
                    );

            List<String> matched = new ArrayList<>();
            List<String> missing = new ArrayList<>();

            for (String keyword : jdKeywords) {

                boolean present;

                if (keyword.contains(" ")) {
                    present = resumeLower.contains(keyword);
                } else {
                    present = resumeTokens.contains(keyword);
                }

                if (present) {
                    matched.add(keyword);
                } else {
                    missing.add(keyword);
                }
            }

            result.setMatchedKeywords(matched);

            result.setMissingKeywords(
                    missing.stream()
                            .limit(20)
                            .collect(Collectors.toList())
            );

            keywordScore = jdKeywords.isEmpty()
                    ? 0
                    : (int) Math.round(
                            100.0 * matched.size() / jdKeywords.size()
                    );

            result.setKeywordMatchScore(keywordScore);
        }

        // ---------------------------------------------------------
        // Quality checks
        // ---------------------------------------------------------

        List<AnalysisCheck> checks = result.getChecks();

        // Contact information
        boolean hasEmail = EMAIL.matcher(resume).find();
        boolean hasPhone = PHONE.matcher(resume).find();

        checks.add(
                new AnalysisCheck(
                        "Contact information",
                        hasEmail && hasPhone,
                        "high",
                        hasEmail && hasPhone
                                ? "Email and phone found."
                                : "Add a clear email and phone number near the top."
                )
        );

        // Resume length
        boolean lengthOk = words.size() >= 250 && words.size() <= 900;

        checks.add(
                new AnalysisCheck(
                        "Length",
                        lengthOk,
                        "medium",
                        words.size() < 250
                                ? "Resume looks thin (" + words.size()
                                        + " words). Aim for 400-800."
                                : words.size() > 900
                                        ? "Resume is long (" + words.size()
                                                + " words). Trim toward 1-2 pages."
                                        : words.size()
                                                + " words - a healthy length."
                )
        );

        // Quantified impact
        boolean hasMetrics = METRIC.matcher(resume).find();

        checks.add(
                new AnalysisCheck(
                        "Quantified impact",
                        hasMetrics,
                        "high",
                        hasMetrics
                                ? "Good - includes numbers/metrics."
                                : "Add measurable results (%, counts, time saved) to strengthen impact."
                )
        );

        // Action verbs
        long actionVerbCount =
                words.stream()
                        .map(String::toLowerCase)
                        .filter(ACTION_VERBS::contains)
                        .count();

        boolean enoughVerbs = actionVerbCount >= 3;

        checks.add(
                new AnalysisCheck(
                        "Action verbs",
                        enoughVerbs,
                        "medium",
                        enoughVerbs
                                ? actionVerbCount
                                        + " strong action verbs detected."
                                : "Start bullets with action verbs (Built, Led, Designed, Reduced...)."
                )
        );

        // Weak phrases
        List<String> foundWeak =
                WEAK_PHRASES.stream()
                        .filter(resumeLower::contains)
                        .toList();

        checks.add(
                new AnalysisCheck(
                        "Filler phrases",
                        foundWeak.isEmpty(),
                        "low",
                        foundWeak.isEmpty()
                                ? "No common filler phrases found."
                                : "Replace weak phrases: "
                                        + String.join(", ", foundWeak)
                                        + "."
                )
        );

        // Standard sections
        boolean hasSections = countSections(resumeLower) >= 3;

        checks.add(
                new AnalysisCheck(
                        "Standard sections",
                        hasSections,
                        "medium",
                        hasSections
                                ? "Recognisable sections present."
                                : "Use clear sections: Summary, Skills, Experience, Education."
                )
        );

        // ---------------------------------------------------------
        // Suggestions
        // ---------------------------------------------------------

        List<String> suggestions =
                checks.stream()
                        .filter(check -> !check.isPassed())
                        .map(AnalysisCheck::getMessage)
                        .collect(Collectors.toList());

        if (hasJd
                && keywordScore < 60
                && !result.getMissingKeywords().isEmpty()) {

            suggestions.add(
                    "Naturally work in missing keywords where true: "
                            + String.join(
                                    ", ",
                                    result.getMissingKeywords()
                                            .stream()
                                            .limit(8)
                                            .toList()
                            )
                            + "."
            );
        }

        result.setSuggestions(suggestions);

        // ---------------------------------------------------------
        // Overall score
        // ---------------------------------------------------------

        int checksScore =
                (int) Math.round(
                        100.0
                                * checks.stream()
                                        .filter(AnalysisCheck::isPassed)
                                        .count()
                                / checks.size()
                );

        int overall;

        if (hasJd) {
            overall =
                    (int) Math.round(
                            0.5 * keywordScore
                                    + 0.5 * checksScore
                    );
        } else {
            overall = checksScore;
        }

        result.setOverallScore(overall);

        return result;
    }

    // ---------------------------------------------------------
    // Tokenize text
    // ---------------------------------------------------------

    private List<String> tokenize(String text) {

        List<String> tokens = new ArrayList<>();

        var matcher = TOKEN.matcher(text);

        while (matcher.find()) {
            tokens.add(matcher.group());
        }

        return tokens;
    }

    // ---------------------------------------------------------
    // Extract keywords from Job Description
    // ---------------------------------------------------------

    private Set<String> extractKeywords(String jd) {

        String lower = jd.toLowerCase();

        Set<String> keywords = new LinkedHashSet<>();

        // Multi-word technical skills
        for (String phrase : SKILL_PHRASES) {

            if (lower.contains(phrase)) {
                keywords.add(phrase);
            }
        }

        // Single-word keywords
        for (String token : tokenize(lower)) {

            if (token.length() < 3) {
                continue;
            }

            if (STOPWORDS.contains(token)) {
                continue;
            }

            keywords.add(token);
        }

        return keywords;
    }

    // ---------------------------------------------------------
    // Count standard resume sections
    // ---------------------------------------------------------

    private int countSections(String resumeLower) {

        String[] markers = {
                "experience",
                "education",
                "skills",
                "summary",
                "objective",
                "projects",
                "certification",
                "achievements",
                "work history"
        };

        return (int) Arrays.stream(markers)
                .filter(resumeLower::contains)
                .count();
    }
}