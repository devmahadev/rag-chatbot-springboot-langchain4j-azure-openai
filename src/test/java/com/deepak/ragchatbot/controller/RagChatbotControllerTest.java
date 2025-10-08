package com.deepak.ragchatbot.controller;

import com.deepak.ragchatbot.service.RagChatbotService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RagChatbotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RagChatbotService ragChatbotService;

    @Test
    void givenValidFile_whenSaveDocumentThrowsIOException_thenReturnsInternalServerError() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.docx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "ACME-RAG-CHECK-42. This is just a RAG test".getBytes());

        when(ragChatbotService.saveDocument(any())).thenThrow(new IOException("Simulated IO Error"));

        mockMvc.perform(multipart("/api/context-chatbot")
                        .file(file)
                        .param("message", "What is ACME-RAG-CHECK-42 and why is it important?"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Simulated IO Error"))
                .andExpect(jsonPath("$.status").value(500));

    }


    @Test
    void givenOversizedFile_whenSaveDocumentThrowsIllegalArgumentException_thenReturnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "large.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[4 * 1024 * 1024]);

        when(ragChatbotService.saveDocument(any())).thenThrow(new IllegalArgumentException("File size must not exceed 3MB"));

        // Mock the service method to throw an exception when called
        mockMvc.perform(multipart("/api/context-chatbot")
                        .file(file)
                        .param("message", "What is ACME-RAG-CHECK-42 and why is it important?"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("File size must not exceed 3MB"))
                .andExpect(jsonPath("$.status").value(400));
    }


    @Test
    void givenUnsupportedFileType_whenSaveSegmentsThrowsUnsupportedOperationException_thenReturnsUnsupportedMediaType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "unsupported.xyz",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "ACME-RAG-CHECK-42. This is just a RAG test".getBytes());

        // Create and stub the mock Resource
        Resource mockResource = Mockito.mock(Resource.class);

        // getFilename() is called in service to determine the file type.
        // Without stubbing it, Mockito tries to resolve it dynamically, which leads to a ClassCastException because it returns a mock object instead of a String.
        // By explicitly stubbing getFilename() to return "unsupported.xyz", we ensure the logic in service behaves as expected during the test.
        when(mockResource.getFilename()).thenReturn("unsupported.xyz"); // ✅ Stub getFilename()

        // Mock service behavior
        when(ragChatbotService.saveDocument(any())).thenReturn(mockResource);
        doThrow(new UnsupportedOperationException("Unsupported file type: .xyz"))
                .when(ragChatbotService).saveSegments(mockResource);

        mockMvc.perform(multipart("/api/context-chatbot")
                        .file(file)
                        .param("message", "What is ACME-RAG-CHECK-42 and why is it important?"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                .andExpect(jsonPath("$.message").value("Unsupported file type: .xyz"))
                .andExpect(jsonPath("$.status").value(415));
    }
}
