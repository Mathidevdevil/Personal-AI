package com.personalassistant.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIToolCall {
    private String id;
    private String toolName;
    private Map<String, Object> arguments;
}
