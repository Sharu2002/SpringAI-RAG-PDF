package com.springai.rag_pdf.Controller;

import org.springframework.ai.chat.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {

    public static final String PROMPT = """
			You are a general AI Assistant
				""";

    private final ChatClient aiClient;

    public ApplicationController(ChatClient aiClient) {
        this.aiClient = aiClient;
    }

    @GetMapping("/ask-ai")
    public ResponseEntity<String> generateAdvice() {
        return ResponseEntity.ok(aiClient.call(PROMPT));
    }
    @GetMapping("/")
    public String home() {
        return "Hello";
    }

}