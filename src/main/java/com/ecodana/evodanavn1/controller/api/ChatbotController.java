package com.ecodana.evodanavn1.controller.api;

import com.ecodana.evodanavn1.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final AIService aiService;

    public ChatbotController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("reply", "Vui lòng nhập câu hỏi."));
        }
        String aiReply = aiService.getAIResponse(userMessage);
        return ResponseEntity.ok(Map.of("reply", aiReply));
    }
}