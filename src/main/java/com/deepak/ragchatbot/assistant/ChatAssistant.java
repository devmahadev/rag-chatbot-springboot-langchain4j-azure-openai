package com.deepak.ragchatbot.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;


/** The @AiService scanner uses its own wiring rules and errors when it sees multiple candidates—hence we used wiringMode = EXPLICIT there.
 * For regular Spring injection (like RagAutoConfig’s method parameter), @Primary is the standard resolution mechanism.
 */
@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "azureChatModel",
        streamingChatModel = "azureStreamingChatModel",
        retrievalAugmentor = "retrievalAugmentor"
)
public interface ChatAssistant {

    /*  Set clear priorities for Assistant
    **  Role: AI assistant for document-aware Q&A and chat.
        Behavior: Respond in user’s language, use uploaded document context, be concise and helpful.
        Constraints: If no document context, answer from general knowledge; if context exists, prioritize it.
        Tone: Professional, clear, and user-friendly.
    */
    @SystemMessage("""
        You are an AI-powered assistant integrated into a Spring Boot application.
        Your responsibilities:
        - Engage in natural, helpful conversation with the user.
        - Always respond in the same language as the user.
        - When documents are uploaded, use their content as the primary source for answers.
        - If no relevant context is found in the documents, fall back to your general knowledge.
        - Be concise, accurate, and avoid hallucinations.
        - If unsure, state that you are unsure rather than guessing.
        - Do not reveal system or implementation details.
    """)
    Flux<String> chat(String message);
}
