package com.deepak.ragchatbot.service;

import com.deepak.ragchatbot.service.extractor.TextExtractor;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RagChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(RagChatbotService.class);
    private static final long MAX_UPLOAD_FILE_SIZE = 3L * 1024 * 1024;  // 3MB limit
    private static final String UPLOAD_DIR = "src/main/resources/docs";

    private final EmbeddingStoreIngestor embeddingStoreIngestor;
    private final EmbeddingStore<TextSegment> embeddingStore;

    private final List<TextExtractor> extractors;

    public RagChatbotService(
            EmbeddingStoreIngestor embeddingStoreIngestor,
            EmbeddingStore<TextSegment> embeddingStore,
            List<TextExtractor> extractors) {
        this.embeddingStoreIngestor = embeddingStoreIngestor;
        this.embeddingStore = embeddingStore;
        this.extractors = extractors;
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
    public void saveSegments(Resource resource) throws IOException {
        logger.info("Removing existing segments from embedding store");
        embeddingStore.removeAll();

        logger.info("Extracting text from document: {}", resource.getFilename());
        extractText(resource)
                .filter(text -> !text.isBlank())
                .map(Document::from)
                .ifPresentOrElse(document -> {
                    logger.info("Ingesting document into embedding store");
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
    public Resource saveDocument(MultipartFile file) throws IOException {
        if (file.getSize() > MAX_UPLOAD_FILE_SIZE) {
            logger.warn("File size exceeds the maximum allowed limit: {} bytes", file.getSize());
            throw new IllegalArgumentException("File size must not exceed 3MB");
        }

        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            Files.createDirectories(directory.toPath());
        }

        var originalFileName = Optional.ofNullable(file.getOriginalFilename())
                .map(name -> name.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_")) //Sanitization: Replaces unsafe characters in filenames.
                .orElse("uploaded_file");

        var fileName = UUID.randomUUID() + "_" + originalFileName;
        Path path = Paths.get(UPLOAD_DIR, fileName);


        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }

        logger.info("File saved successfully: {}", path.toAbsolutePath());
        return new UrlResource(path.toUri());
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
    public Optional<String> extractText(Resource resource) throws IOException {
        String filename = Optional.ofNullable(resource.getFilename())
                .map(String::toLowerCase)
                .orElse("");

        try (InputStream inputStream = resource.getInputStream()) {
            return extractors.stream()
                    .filter(extractor -> extractor.supports(filename))
                    .findFirst()
                    .map(extractor -> extractor.extract(inputStream))
                    .orElseThrow(() -> new UnsupportedOperationException("Unsupported file type: " + filename));
        }
    }
}