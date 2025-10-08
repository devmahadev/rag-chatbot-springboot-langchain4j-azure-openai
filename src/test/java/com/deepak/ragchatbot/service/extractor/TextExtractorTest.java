package com.deepak.ragchatbot.service.extractor;

import com.deepak.ragchatbot.service.RagChatbotService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/** Test covers:
 * - PDF and DOCX creation in-memory: avoids file I/O.
 * - Strategy pattern validation: ensures correct extractor is chosen.
 * - Error handling: verifies fallback behavior for unsupported types.
 */
public class TextExtractorTest {
    private RagChatbotService ragChatbotService;

    EmbeddingStoreIngestor mockIngestor = mock(EmbeddingStoreIngestor.class);
    EmbeddingStore<TextSegment> mockStore = mock(EmbeddingStore.class);


    @BeforeEach
    void setUp() {
        ragChatbotService = new RagChatbotService(
                mockIngestor,
                mockStore,
                List.of(
                        new PdfTextExtractor(),
                        new DocxTextExtractor()
                ));
    }

    @Test
    void testPdfTextExtraction() throws IOException {
        var out = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.beginText();
                contentStream.newLineAtOffset(100, 700);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.showText("Hello This is Test for Text Extractor");
                contentStream.endText();
            }
            doc.save(out);
        }

        Resource resource = new ByteArrayResource(out.toByteArray()) {
            @Override
            public String getFilename() {
                return "test.pdf";
            }
        };

        Optional<String> extractedText = ragChatbotService.extractText(resource);
        assertTrue(extractedText.get().contains("Hello This is Test for Text Extractor"));
    }

    @Test
    void testDocxTextExtraction() throws IOException {
        byte[] bytes;
        try (var doc = new XWPFDocument()) {
            var r = doc.createParagraph().createRun();
            r.setText("Hello This is Test for Text Extractor");
            try (var out = new ByteArrayOutputStream()) {
                doc.write(out);
                bytes = out.toByteArray();
            }
        }

        Resource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return "test.docx";
            }
        };

        Optional<String> extractedText = ragChatbotService.extractText(resource);
        assertTrue(extractedText.get().contains("Hello This is Test for Text Extractor"));
    }

    @Test
    void testUnsupportedFileType() {
        Resource resource = new ByteArrayResource("dummy content".getBytes()) {
            @Override
            public String getFilename() {
                return "test.txt";
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream("dummy content".getBytes());
            }
        };

        UnsupportedOperationException exception =
                assertThrows(UnsupportedOperationException.class, () ->
                        ragChatbotService.extractText(resource)
                );

        assertEquals("Unsupported file type: test.txt", exception.getMessage());
    }
}
