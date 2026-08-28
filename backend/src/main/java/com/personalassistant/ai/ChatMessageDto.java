package com.personalassistant.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private String id;
    private MessageRole role;
    private String content;
    private String toolCallJson;
    private Instant createdAt;

    public static ChatMessageDto fromEntity(ChatMessage message) {
        return ChatMessageDto.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .toolCallJson(message.getToolCallJson())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
