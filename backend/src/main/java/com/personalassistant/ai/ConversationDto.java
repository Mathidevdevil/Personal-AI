package com.personalassistant.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    private String id;
    private String title;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ChatMessageDto> messages;

    public static ConversationDto fromEntity(Conversation conversation) {
        List<ChatMessageDto> msgList = conversation.getMessages() != null
                ? conversation.getMessages().stream().map(ChatMessageDto::fromEntity).toList()
                : List.of();

        return ConversationDto.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .messages(msgList)
                .build();
    }
}
