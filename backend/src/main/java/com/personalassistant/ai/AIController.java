package com.personalassistant.ai;

import com.personalassistant.common.ApiResponse;
import com.personalassistant.common.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final ConversationService conversationService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AIChatResponse>> chat(@Valid @RequestBody AIChatRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        AIChatResponse response = conversationService.handleUserChat(userId, request);
        return ResponseEntity.ok(ApiResponse.success("AI response generated", response));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationDto>>> getConversations() {
        String userId = SecurityUtils.getCurrentUserId();
        List<ConversationDto> list = conversationService.getUserConversations(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<ConversationDto>> getConversationById(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        ConversationDto conv = conversationService.getConversationById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(conv));
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        conversationService.deleteConversation(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted successfully", null));
    }
}
