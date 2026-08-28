package com.personalassistant.ai;

import com.personalassistant.common.ApiResponse;
import com.personalassistant.common.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationDto>>> getConversations() {
        String userId = SecurityUtils.getCurrentUserId();
        List<ConversationDto> list = conversationService.getUserConversations(userId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConversationDto>> getConversationById(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        ConversationDto conv = conversationService.getConversationById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(conv));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        conversationService.deleteConversation(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted successfully", null));
    }
}
