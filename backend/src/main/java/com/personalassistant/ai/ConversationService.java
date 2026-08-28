package com.personalassistant.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalassistant.common.ResourceNotFoundException;
import com.personalassistant.user.User;
import com.personalassistant.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final AIService aiService;
    private final AIContextService aiContextService;
    private final AIToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    @Transactional
    public AIChatResponse handleUserChat(String userId, AIChatRequest request) {
        User user = userService.getUserById(userId);

        Conversation conversation;
        if (request.getConversationId() != null && !request.getConversationId().isBlank()) {
            conversation = conversationRepository.findByIdAndUser(request.getConversationId(), user)
                    .orElseGet(() -> createNewConversation(user, request.getMessage()));
        } else {
            conversation = createNewConversation(user, request.getMessage());
        }

        // Save User message
        ChatMessage userMsg = ChatMessage.builder()
                .conversation(conversation)
                .role(MessageRole.USER)
                .content(request.getMessage().trim())
                .build();
        chatMessageRepository.save(userMsg);

        // Build context & load history
        String systemContext = aiContextService.buildSystemContext(userId);
        List<ChatMessageDto> history = chatMessageRepository.findByConversationOrderByCreatedAtAsc(conversation)
                .stream().map(ChatMessageDto::fromEntity).toList();

        AIRequest aiRequest = AIRequest.builder()
                .userId(userId)
                .prompt(request.getMessage().trim())
                .systemContext(systemContext)
                .history(history)
                .tools(toolRegistry.getAllTools())
                .build();

        // Process AI chat & tool execution
        AIResponse aiResponse = aiService.chat(aiRequest);

        String toolJson = null;
        if (aiResponse.getToolResults() != null && !aiResponse.getToolResults().isEmpty()) {
            try {
                toolJson = objectMapper.writeValueAsString(aiResponse.getToolResults());
            } catch (Exception ignored) {}
        }

        // Save Assistant reply
        ChatMessage assistantMsg = ChatMessage.builder()
                .conversation(conversation)
                .role(MessageRole.ASSISTANT)
                .content(aiResponse.getReply())
                .toolCallJson(toolJson)
                .build();
        ChatMessage savedAssistantMsg = chatMessageRepository.save(assistantMsg);

        // Update conversation updated_at
        conversationRepository.save(conversation);

        return AIChatResponse.builder()
                .conversationId(conversation.getId())
                .conversationTitle(conversation.getTitle())
                .message(ChatMessageDto.fromEntity(savedAssistantMsg))
                .toolResults(aiResponse.getToolResults())
                .build();
    }

    @Transactional
    public Conversation createNewConversation(User user, String initialMessage) {
        String title = initialMessage.length() > 40 ? initialMessage.substring(0, 37) + "..." : initialMessage;
        Conversation conv = Conversation.builder()
                .user(user)
                .title(title.trim())
                .build();
        return conversationRepository.save(conv);
    }

    @Transactional(readOnly = true)
    public List<ConversationDto> getUserConversations(String userId) {
        User user = userService.getUserById(userId);
        return conversationRepository.findByUserOrderByUpdatedAtDesc(user).stream()
                .map(ConversationDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationDto getConversationById(String userId, String conversationId) {
        User user = userService.getUserById(userId);
        Conversation conv = conversationRepository.findByIdAndUser(conversationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        return ConversationDto.fromEntity(conv);
    }

    @Transactional
    public void deleteConversation(String userId, String conversationId) {
        User user = userService.getUserById(userId);
        Conversation conv = conversationRepository.findByIdAndUser(conversationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        conversationRepository.delete(conv);
    }
}
