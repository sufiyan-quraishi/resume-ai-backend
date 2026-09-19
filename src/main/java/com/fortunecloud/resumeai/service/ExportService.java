package com.fortunecloud.resumeai.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts the generated Markdown into downloadable PDF and DOCX files.
 * A small, purpose-built Markdown subset is supported: #/##/### headings,
 * - or * bullets, and **bold** inline spans. Anything else is treated as text.
 */
@Service
public class ExportService {

    private static final Color ACCENT = new Color(226, 85, 47); // matches the UI accent

    // ----------------------------------------------------------------- PDF

    public byte[] toPdf(String markdown) {
        Document doc = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        for (String raw : markdown.split("\n")) {
            String line = raw.stripTrailing();
            if (line.isBlank()) {
                doc.add(new Paragraph(" "));
                continue;
            }
            if (line.startsWith("### ")) {
                doc.add(heading(line.substring(4), 12.5f, Color.DARK_GRAY));
            } else if (line.startsWith("## ")) {
                doc.add(heading(line.substring(3), 14f, ACCENT));
            } else if (line.startsWith("# ")) {
                doc.add(heading(line.substring(2), 20f, Color.BLACK));
            } else if (isBullet(line)) {
                Paragraph p = new Paragraph();
                p.setIndentationLeft(16f);
                p.add(new Phrase("\u2022  "));
                addInline(p, line.substring(2));
                doc.add(p);
            } else {
                Paragraph p = new Paragraph();
                addInline(p, line);
                doc.add(p);
            }
        }
        doc.close();
        return out.toByteArray();
    }

    private Paragraph heading(String text, float size, Color color) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, size, color);
        Paragraph p = new Paragraph(stripBold(text), font);
        p.setSpacingBefore(8f);
        p.setSpacingAfter(2f);
        return p;
    }

    /** Adds text to a PDF paragraph, honouring **bold** spans. */
    private void addInline(Paragraph p, String text) {
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, Color.BLACK);
        Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f, Color.BLACK);
        for (Segment seg : parseBold(text)) {
            p.add(new Phrase(seg.text, seg.bold ? bold : normal));
        }
        p.setLeading(15f);
    }

    // ---------------------------------------------------------------- DOCX

    public byte[] toDocx(String markdown) throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            for (String raw : markdown.split("\n")) {
                String line = raw.stripTrailing();
                if (line.isBlank()) {
                    continue;
                }
                if (line.startsWith("### ")) {
                    docxHeading(doc, line.substring(4), 13, "333333");
                } else if (line.startsWith("## ")) {
                    docxHeading(doc, line.substring(3), 15, "E2552F");
                } else if (line.startsWith("# ")) {
                    docxHeading(doc, line.substring(2), 24, "111111");
                } else if (isBullet(line)) {
                    XWPFParagraph p = doc.createParagraph();
                    p.setIndentationLeft(360);
                    XWPFRun bullet = p.createRun();
                    bullet.setText("\u2022  ");
                    addDocxInline(p, line.substring(2), 11);
                } else {
                    XWPFParagraph p = doc.createParagraph();
                    addDocxInline(p, line, 11);
                }
            }
            doc.write(out);
            return out.toByteArray();
        }
    }

    private void docxHeading(XWPFDocument doc, String text, int size, String hexColor) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(120);
        XWPFRun run = p.createRun();
        run.setText(stripBold(text));
        run.setBold(true);
        run.setFontSize(size);
        run.setColor(hexColor);
    }

    private void addDocxInline(XWPFParagraph p, String text, int size) {
        for (Segment seg : parseBold(text)) {
            XWPFRun run = p.createRun();
            run.setText(seg.text);
            run.setBold(seg.bold);
            run.setFontSize(size);
        }
    }

    // -------------------------------------------------- shared markdown bits

    private boolean isBullet(String line) {
        return line.startsWith("- ") || line.startsWith("* ");
    }

    private String stripBold(String s) {
        return s.replace("**", "");
    }

    /** Splits a line into bold / non-bold segments based on **markers**. */
    private List<Segment> parseBold(String text) {
        List<Segment> segments = new ArrayList<>();
        int i = 0;
        boolean bold = false;
        StringBuilder current = new StringBuilder();
        while (i < text.length()) {
            if (i + 1 < text.length() && text.charAt(i) == '*' && text.charAt(i + 1) == '*') {
                if (current.length() > 0) {
                    segments.add(new Segment(current.toString(), bold));
                    current.setLength(0);
                }
                bold = !bold;
                i += 2;
            } else {
                current.append(text.charAt(i));
                i++;
            }
        }
        if (current.length() > 0) segments.add(new Segment(current.toString(), bold));
        if (segments.isEmpty()) segments.add(new Segment("", false));
        return segments;
    }

    private record Segment(String text, boolean bold) {
    }
}
