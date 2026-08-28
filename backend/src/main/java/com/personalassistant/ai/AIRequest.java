package com.personalassistant.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {
    private String userId;
    private String prompt;
    private String systemContext;
    private List<ChatMessageDto> history;
    private List<AIToolDefinition> tools;
}
