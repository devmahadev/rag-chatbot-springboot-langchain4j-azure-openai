package com.deepak.ragchatbot.service.extractor;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class DocxTextExtractor implements TextExtractor{
    private static final Logger logger = LoggerFactory.getLogger(PdfTextExtractor.class);
    /**
     * @param filename
     * @return
     */
    @Override
    public boolean supports(String filename) {
        return filename.toLowerCase().endsWith(".docx");
    }

    /**
     * Extracts text from a DOCX file.
     * Steps:
     * - Loads the DOCX into XWPFDocument.
     * - Uses XWPFWordExtractor to get text content.
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    @Override
    public Optional<String> extract(InputStream inputStream) {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(document);
            return Optional.ofNullable(xwpfWordExtractor.getText());
        } catch (IOException e) {
            logger.error("Failed to extract DOCX text", e);
            return Optional.empty();
        }
    }
}
