package com.deepak.ragchatbot.service;

import dev.langchain4j.data.document.BlankDocumentException;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class RagChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(RagChatbotService.class);
    private static final long MAX_UPLOAD_FILE_SIZE = 3L * 1024 * 1024;  // 3MB limit
    private static final String UPLOAD_DIR = "src/main/resources/docs";

    private final EmbeddingStoreIngestor embeddingStoreIngestor;
    private final EmbeddingStore<TextSegment> embeddingStore;


    public RagChatbotService(EmbeddingStoreIngestor embeddingStoreIngestor, EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingStoreIngestor = embeddingStoreIngestor;
        this.embeddingStore = embeddingStore;
    }

    /**
     * Clears existing embeddings and ingests new document content into the embedding store.
     * Steps:
     * - Removes all previous segments from embeddingStore.
     * - Extracts text from the uploaded document (extractText()).
     * - Converts text into a Document object.
     * - Uses embeddingStoreIngestor to generate embeddings and store them.
     *
     * @param resource
     */
    public void saveSegments(Resource resource) {
        logger.info("Removing segments");
        embeddingStore.removeAll();

        try {
            logger.info("Extracting text from documents");
            String extractedText = extractText(resource);

            if (extractedText == null || extractedText.trim().isEmpty()) {
                throw new BlankDocumentException();
            }

            Document document = Document.from(extractedText);
            logger.info("Ingesting documents");
            embeddingStoreIngestor.ingest(document);

            logger.info("Document Ingested Successfully");

        } catch (UnsupportedOperationException unsupportedOperationException) {
            logger.error("Unsupported file type: {}", resource.getFilename());
        } catch (Exception exception) {
            logger.warn("Document is empty or unreadable: {}", resource.getFilename());
        }
    }

    /**
     * Saves the uploaded file to a local directory and returns it as a Resource.
     * Steps:
     * - Validates file size (max 3MB).
     * - Creates upload directory if missing.
     * - Generates a unique filename and saves the file.
     * - Returns a UrlResource pointing to the saved file.
     *
     * @param file
     * @return
     */
    public Resource saveDocument(MultipartFile file) {
        try {
            if (file.getSize() > MAX_UPLOAD_FILE_SIZE) {
                throw new IllegalArgumentException("File size must not exceed 3MB");
            }

            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR, fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return new UrlResource(path.toUri());
        } catch (IOException e) {   // TODO: Refactor Exception Handling
            logger.error("Failed to save document", e);
            throw new RuntimeException(e);
        }

    }

    /**
     * Reads the file and extracts text based on its type.
     * Steps:
     * - Checks file extension (.pdf or .docx).
     * - Calls extractPdfText() or extractDocxText() accordingly.
     * - Throws UnsupportedOperationException for unsupported types.
     *
     * @param resource
     * @return
     * @throws IOException
     */
    private String extractText(Resource resource) throws IOException {
        String filename = resource.getFilename().toLowerCase(); // TODO: Can lead to NPE

        try (InputStream inputStream = resource.getInputStream()) {
            if (filename.endsWith(".pdf")) {
                return extractPdfText(inputStream);
            } else if (filename.endsWith(".docx")) {
                return extractDocxText(inputStream);
            } else {
                throw new UnsupportedOperationException("Unsupported file type: " + filename);
            }
        }
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
    private String extractDocxText(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(document);
            return xwpfWordExtractor.getText();
        }
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
    private String extractPdfText(InputStream inputStream) throws IOException {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            return pdfTextStripper.getText(document);
        }
    }
}