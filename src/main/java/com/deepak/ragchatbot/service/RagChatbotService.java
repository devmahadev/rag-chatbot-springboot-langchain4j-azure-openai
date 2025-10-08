package com.deepak.ragchatbot.service;

import module langchain4j.core;
import module org.apache.pdfbox;
import module org.apache.poi.ooxml;
import module spring.core;
import module spring.web;
import module spring.context;
import module java.base;
import java.nio.file.Path;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        logger.info("Extracting text from documents");
        extractText(resource)
                .filter(text -> !text.isBlank())
                .map(Document::from)
                .ifPresentOrElse(document -> {
                    logger.info("Ingesting documents");
                    embeddingStoreIngestor.ingest(document);
                    logger.info("Document Ingested Successfully");
                }, () -> logger.warn("Document is empty or unreadable: {}", resource.getFilename()));
    }

    /**
     * Saves the uploaded file to a local directory and returns it as a Resource.
     * Steps:
     * - Validates file size (max 3MB).
     * - Creates upload directory if missing.
     * - Generates a unique filename and saves the file.
     * - Returns a UrlResource pointing to the saved file.
     * <p>
     * Refactoring provides:
     * - Sanitization: Replaces unsafe characters in filenames.
     * - Optional return: Avoids throwing exceptions, making it safer for consumers.
     * - Logging: Provides context for both warnings and errors.
     * - Modern APIs: Uses Path.of, Files.createDirectories, and try-with-resources.
     *
     * @param file
     * @return
     */
    public Optional<Resource> saveDocument(MultipartFile file) {
        if (file.getSize() > MAX_UPLOAD_FILE_SIZE) {
            logger.warn("File size exceeds the maximum allowed limit: {} bytes", file.getSize());
            throw new IllegalArgumentException("File size must not exceed 3MB");
        }

        try {
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            var originalFileName = Optional.ofNullable(file.getOriginalFilename())
                    .map(name -> name.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_")) //Sanitization: Replaces unsafe characters in filenames.
                    .orElse("uploaded_file");
            var fileName = UUID.randomUUID() + "_" + originalFileName;

            Path path = Paths.get(UPLOAD_DIR, fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return Optional.of(new UrlResource(path.toUri()));
        } catch (IOException e) {
            logger.error("Failed to save document: {}", file.getOriginalFilename(), e);
            return Optional.empty();
        }
    }

    /**
     * Reads the file and extracts text based on its type.
     * Steps:
     * - Checks file extension (.pdf or .docx).
     * - Calls extractPdfText() or extractDocxText() accordingly.
     * - Throws UnsupportedOperationException for unsupported types.
     * <p>
     * <p>
     * Refactoring highlights:
     * - Null safety: Uses Optional.ofNullable(...).orElse("") to avoid NPE.
     * - Switch expression: Cleaner branching logic with switch and pattern matching.
     * - Graceful fallback: Returns Optional.empty() instead of throwing exceptions.
     *
     * @param resource
     * @return
     * @throws IOException
     */
    private Optional<String> extractText(Resource resource) {
        String filename = Optional.ofNullable(resource.getFilename())
                .map(String::toLowerCase)
                .orElse("");

        try (InputStream inputStream = resource.getInputStream()) {
            return switch (filename) {
                case String f when f.endsWith(".pdf") -> extractPdfText(inputStream);
                case String f when f.endsWith(".docx") -> extractDocxText(inputStream);
                default -> {
                    logger.error("Unsupported file type: " + filename);
                    yield Optional.empty();
                }
            };
        } catch (IOException ex) {
            logger.error("Error reading resource: " + ex.getMessage());
            return Optional.empty();
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
    private Optional<String> extractDocxText(InputStream inputStream) {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(document);
            return Optional.ofNullable(xwpfWordExtractor.getText());
        } catch (IOException e) {
            logger.error("Failed to extract DOCX text", e);
            return Optional.empty();
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
    private Optional<String> extractPdfText(InputStream inputStream) {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            return Optional.ofNullable(pdfTextStripper.getText(document));
        } catch (IOException e) {
            logger.error("Failed to extract PDF text", e);
            return Optional.empty();
        }
    }
}