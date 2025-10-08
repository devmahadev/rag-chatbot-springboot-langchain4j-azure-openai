package com.deepak.ragchatbot.service.extractor;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class PdfTextExtractor implements TextExtractor{
    private static final Logger logger = LoggerFactory.getLogger(PdfTextExtractor.class);

    /**
     * @param filename
     * @return
     */
    @Override
    public boolean supports(String filename) {
        return filename.toLowerCase().endsWith(".pdf");
    }

    /**
     * Extracts text from a PDF file.
     * Steps:
     * - Loads PDF using PDFBox Loader.
     * - Uses PDFTextStripper to extract text.
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    @Override
    public Optional<String>  extract(InputStream inputStream) {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            return Optional.ofNullable(pdfTextStripper.getText(document));
        } catch (IOException e) {
            logger.error("Failed to extract PDF text", e);
            return Optional.empty();
        }
    }
}
