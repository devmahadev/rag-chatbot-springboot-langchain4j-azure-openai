package com.deepak.ragchatbot.controller;


import module spring.core;
import module spring.web;
import module java.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import com.deepak.ragchatbot.assistant.ChatAssistant;
import com.deepak.ragchatbot.service.RagChatbotService;


@RestController
@RequestMapping("/api")
public class RagChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(RagChatbotController.class);
    private final ChatAssistant chatAssistant;
    private final RagChatbotService ragChatbotService;

    public RagChatbotController(ChatAssistant chatAssistant, RagChatbotService ragChatbotService) {
        this.chatAssistant = chatAssistant;
        this.ragChatbotService = ragChatbotService;
    }

    @GetMapping(value = "/chatbot", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatbot(@RequestParam String message) {
        return chatAssistant.chat(message);
    }

    @PostMapping(value = "/context-chatbot", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Flux<String>> load(@RequestParam(defaultValue = "What is the content of the document?") String message,
                             @RequestParam(value = "file", required = false) MultipartFile file) {

        // 1. File Validation: if no file is uploaded then proceed with regular chat model
        if (file == null || file.isEmpty()) {
            logger.info("File is empty or not provided.");
            return ResponseEntity.ok(chatAssistant.chat(message));
        }

        // 2. Save the uploaded file to directory
        logger.info("Uploading document.....");
        Optional<Resource> optionalResource = ragChatbotService.saveDocument(file);

        if (optionalResource.isEmpty()) {
            logger.warn("Failed to save document.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Flux.just("Failed to process the uploaded document."));
        }
        Resource resource = optionalResource.get();
        logger.info("Document uploaded successfully.");

        // 3. Extract text segments from the document and store them for retrieval
        logger.info("Extracting and saving documents segments to DB.....");
        ragChatbotService.saveSegments(resource);
        logger.info("Document segments saved.");

        // 4. Generate a chat response using uploaded document as context conversation
        logger.info("Generating contextual chat response");
        return ResponseEntity.ok(chatAssistant.chat(message));
    }
}
