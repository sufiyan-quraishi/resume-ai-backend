package com.fortunecloud.resumeai.service;

import com.fortunecloud.resumeai.dto.CoverLetterRequest;
import com.fortunecloud.resumeai.dto.GeneratedContent;
import com.fortunecloud.resumeai.dto.ResumeRequest;
import com.fortunecloud.resumeai.exception.AiNotConfiguredException;
import com.fortunecloud.resumeai.model.Education;
import com.fortunecloud.resumeai.model.Experience;
import com.fortunecloud.resumeai.model.ProjectItem;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Turns user input into a Markdown resume or cover letter.
 * Uses the LLM when configured; otherwise builds the document from templates
 * so the app works end-to-end with no API key.
 */
@Service
public class ResumeService {

    private final AiClient aiClient;
    
    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);

    public ResumeService(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    // ---------------------------------------------------------------- Resume

    public GeneratedContent generateResume(ResumeRequest req) {
        try {
            String markdown = aiClient.complete(resumeSystemPrompt(req), resumeUserPrompt(req));
            return new GeneratedContent("RESUME", stripCodeFences(markdown), true);
        } catch (AiNotConfiguredException e) {
            log.info("Generating resume with offline template ({})", e.getMessage());
            return new GeneratedContent("RESUME", templateResume(req), false);
        }
    }

    private String resumeSystemPrompt(ResumeRequest req) {
        return """
                You are an expert technical resume writer who produces ATS-friendly resumes.
                Write the resume in %s. Output GitHub-flavoured Markdown only - no commentary,
                no code fences. Use a single H1 for the name, H2 for sections, bullet points for
                achievements. Start each experience bullet with a strong action verb and quantify
                impact where the input allows. Keep it concise and one to two pages of content.
                Do not invent employers, dates, or degrees that are not provided.
                """.formatted(orDefault(req.getLanguage(), "English"));
    }

    private String resumeUserPrompt(ResumeRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("Create a resume tailored for the role: ")
                .append(orDefault(req.getTargetRole(), "the candidate's field")).append(".\n\n");
        sb.append("CANDIDATE DETAILS\n");
        sb.append("Name: ").append(req.getFullName()).append('\n');
        appendIf(sb, "Email", req.getEmail());
        appendIf(sb, "Phone", req.getPhone());
        appendIf(sb, "Location", req.getLocation());
        appendIf(sb, "LinkedIn", req.getLinkedIn());
        appendIf(sb, "GitHub", req.getGithub());
        appendIf(sb, "Portfolio", req.getPortfolio());
        appendIf(sb, "Existing summary", req.getSummary());

        if (!req.getSkills().isEmpty()) {
            sb.append("Skills: ").append(String.join(", ", req.getSkills())).append('\n');
        }
        if (!req.getCertifications().isEmpty()) {
            sb.append("Certifications: ").append(String.join(", ", req.getCertifications())).append('\n');
        }
        if (!req.getAchievements().isEmpty()) {
            sb.append("Achievements: ").append(String.join("; ", req.getAchievements())).append('\n');
        }

        if (!req.getExperience().isEmpty()) {
            sb.append("\nEXPERIENCE\n");
            for (Experience e : req.getExperience()) {
                sb.append("- ").append(orDefault(e.getJobTitle(), "Role"))
                        .append(" at ").append(orDefault(e.getCompany(), "Company"))
                        .append(" (").append(orDefault(e.getDuration(), "")).append(")\n");
                for (String r : e.getResponsibilities()) {
                    if (StringUtils.hasText(r)) sb.append("    * ").append(r).append('\n');
                }
            }
        }

        if (!req.getProjects().isEmpty()) {
            sb.append("\nPROJECTS\n");
            for (ProjectItem p : req.getProjects()) {
                sb.append("- ").append(orDefault(p.getName(), "Project"));
                if (StringUtils.hasText(p.getTechStack())) sb.append(" [").append(p.getTechStack()).append(']');
                if (StringUtils.hasText(p.getDescription())) sb.append(": ").append(p.getDescription());
                sb.append('\n');
            }
        }

        if (!req.getEducation().isEmpty()) {
            sb.append("\nEDUCATION\n");
            for (Education ed : req.getEducation()) {
                sb.append("- ").append(orDefault(ed.getDegree(), "Degree"))
                        .append(", ").append(orDefault(ed.getInstitution(), ""))
                        .append(" (").append(orDefault(ed.getYear(), "")).append(") ")
                        .append(orDefault(ed.getScore(), "")).append('\n');
            }
        }

        if (StringUtils.hasText(req.getJobDescription())) {
            sb.append("\nTAILOR AGAINST THIS JOB DESCRIPTION:\n").append(req.getJobDescription()).append('\n');
        }
        return sb.toString();
    }

    // ----------------------------------------------------------- Cover letter

    public GeneratedContent generateCoverLetter(CoverLetterRequest req) {
        try {
            String markdown = aiClient.complete(coverSystemPrompt(req), coverUserPrompt(req));
            return new GeneratedContent("COVER_LETTER", stripCodeFences(markdown), true);
        } catch (AiNotConfiguredException e) {
            log.info("Generating cover letter with offline template ({})", e.getMessage());
            return new GeneratedContent("COVER_LETTER", templateCoverLetter(req), false);
        }
    }

    private String coverSystemPrompt(CoverLetterRequest req) {
        return """
                You are a professional career writer. Write a tailored cover letter in %s with a
                %s tone. Output Markdown only - no commentary or code fences. Keep it to 3-4 short
                paragraphs: a hook that names the role and company, a body that maps the candidate's
                strengths to the job description, and a confident closing with a call to action.
                Do not fabricate experience that is not provided.
                """.formatted(orDefault(req.getLanguage(), "English"), orDefault(req.getTone(), "professional"));
    }

    private String coverUserPrompt(CoverLetterRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("Applicant: ").append(req.getFullName()).append('\n');
        appendIf(sb, "Email", req.getEmail());
        appendIf(sb, "Phone", req.getPhone());
        sb.append("Applying for: ").append(orDefault(req.getJobTitle(), "the open role"))
                .append(" at ").append(req.getCompanyName()).append('\n');
        appendIf(sb, "Hiring manager", req.getHiringManager());
        appendIf(sb, "Years of experience", req.getYearsOfExperience());
        if (!req.getSkills().isEmpty()) {
            sb.append("Key skills: ").append(String.join(", ", req.getSkills())).append('\n');
        }
        if (!req.getHighlights().isEmpty()) {
            sb.append("Highlights to weave in: ").append(String.join("; ", req.getHighlights())).append('\n');
        }
        sb.append("\nJOB DESCRIPTION:\n").append(req.getJobDescription()).append('\n');
        return sb.toString();
    }

    // ------------------------------------------------------ Offline templates

    private String templateResume(ResumeRequest r) {
        StringBuilder md = new StringBuilder();
        md.append("# ").append(r.getFullName()).append("\n\n");
        if (StringUtils.hasText(r.getTargetRole())) {
            md.append("**").append(r.getTargetRole()).append("**\n\n");
        }

        // Contact line
        StringBuilder contact = new StringBuilder();
        joinContact(contact, r.getEmail());
        joinContact(contact, r.getPhone());
        joinContact(contact, r.getLocation());
        joinContact(contact, r.getLinkedIn());
        joinContact(contact, r.getGithub());
        joinContact(contact, r.getPortfolio());
        if (contact.length() > 0) md.append(contact).append("\n\n");

        // Summary
        md.append("## Professional Summary\n\n");
        if (StringUtils.hasText(r.getSummary())) {
            md.append(r.getSummary()).append("\n\n");
        } else {
            md.append(buildSummary(r)).append("\n\n");
        }

        if (!r.getSkills().isEmpty()) {
            md.append("## Skills\n\n");
            md.append(String.join(" \u00b7 ", r.getSkills())).append("\n\n");
        }

        if (!r.getExperience().isEmpty()) {
            md.append("## Experience\n\n");
            for (Experience e : r.getExperience()) {
                md.append("### ").append(orDefault(e.getJobTitle(), "Role"))
                        .append(" \u2014 ").append(orDefault(e.getCompany(), "Company")).append('\n');
                String meta = joinNonBlank(" | ", e.getDuration(), e.getLocation());
                if (StringUtils.hasText(meta)) md.append("*").append(meta).append("*\n");
                md.append('\n');
                for (String resp : e.getResponsibilities()) {
                    if (StringUtils.hasText(resp)) md.append("- ").append(resp).append('\n');
                }
                md.append('\n');
            }
        }

        if (!r.getProjects().isEmpty()) {
            md.append("## Projects\n\n");
            for (ProjectItem p : r.getProjects()) {
                md.append("- **").append(orDefault(p.getName(), "Project")).append("**");
                if (StringUtils.hasText(p.getTechStack())) md.append(" (").append(p.getTechStack()).append(")");
                if (StringUtils.hasText(p.getDescription())) md.append(" \u2014 ").append(p.getDescription());
                md.append('\n');
            }
            md.append('\n');
        }

        if (!r.getEducation().isEmpty()) {
            md.append("## Education\n\n");
            for (Education ed : r.getEducation()) {
                md.append("- **").append(orDefault(ed.getDegree(), "Degree")).append("**, ")
                        .append(orDefault(ed.getInstitution(), ""));
                String meta = joinNonBlank(" | ", ed.getYear(), ed.getScore());
                if (StringUtils.hasText(meta)) md.append(" (").append(meta).append(")");
                md.append('\n');
            }
            md.append('\n');
        }

        if (!r.getCertifications().isEmpty()) {
            md.append("## Certifications\n\n");
            for (String c : r.getCertifications()) md.append("- ").append(c).append('\n');
            md.append('\n');
        }

        if (!r.getAchievements().isEmpty()) {
            md.append("## Achievements\n\n");
            for (String a : r.getAchievements()) md.append("- ").append(a).append('\n');
            md.append('\n');
        }
        return md.toString().trim();
    }

    private String buildSummary(ResumeRequest r) {
        String role = orDefault(r.getTargetRole(), "Software Professional");
        String skills = r.getSkills().isEmpty()
                ? "a strong technical foundation"
                : String.join(", ", r.getSkills().subList(0, Math.min(5, r.getSkills().size())));
        return String.format(
                "%s with hands-on experience delivering production software. Skilled in %s. "
                        + "Focused on writing clean, maintainable code and shipping features that create measurable impact.",
                role, skills);
    }

    private String templateCoverLetter(CoverLetterRequest c) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
        String greeting = StringUtils.hasText(c.getHiringManager())
                ? "Dear " + c.getHiringManager() + ","
                : "Dear Hiring Team,";
        String role = orDefault(c.getJobTitle(), "the advertised position");
        String skills = c.getSkills().isEmpty() ? "" :
                " My core strengths include " + String.join(", ", c.getSkills()) + ".";
        String highlights = c.getHighlights().isEmpty() ? "" :
                " In recent work I " + String.join("; I ", c.getHighlights()) + ".";
        String yrs = StringUtils.hasText(c.getYearsOfExperience())
                ? "With " + c.getYearsOfExperience() + " years of experience, " : "";

        return ("**" + c.getFullName() + "**  \n"
                + joinNonBlank(" \u00b7 ", c.getEmail(), c.getPhone()) + "\n\n"
                + date + "\n\n"
                + greeting + "\n\n"
                + "I am writing to apply for **" + role + "** at **" + c.getCompanyName()
                + "**. The role aligns closely with my background, and I am excited by the opportunity to contribute."
                + skills + "\n\n"
                + yrs + "I bring a track record of building reliable software and collaborating across teams to deliver results."
                + highlights + " I am confident I can bring the same focus and ownership to your team.\n\n"
                + "Thank you for considering my application. I would welcome the chance to discuss how my experience fits "
                + c.getCompanyName() + "'s goals.\n\n"
                + "Sincerely,  \n" + c.getFullName()).trim();
    }

    // ------------------------------------------------------------- Helpers

    private static void appendIf(StringBuilder sb, String label, String value) {
        if (StringUtils.hasText(value)) sb.append(label).append(": ").append(value).append('\n');
    }

    private static void joinContact(StringBuilder sb, String value) {
        if (StringUtils.hasText(value)) {
            if (sb.length() > 0) sb.append(" \u00b7 ");
            sb.append(value);
        }
    }

    private static String joinNonBlank(String sep, String... parts) {
        return java.util.Arrays.stream(parts).filter(StringUtils::hasText)
                .reduce((a, b) -> a + sep + b).orElse("");
    }

    private static String orDefault(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    /** LLMs sometimes wrap output in ```markdown fences; remove them. */
    private static String stripCodeFences(String s) {
        String t = s.trim();
        if (t.startsWith("```")) {
            int firstNl = t.indexOf('\n');
            if (firstNl > 0) t = t.substring(firstNl + 1);
            if (t.endsWith("```")) t = t.substring(0, t.length() - 3);
        }
        return t.trim();
    }
}
