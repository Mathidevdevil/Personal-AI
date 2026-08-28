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
public class AIToolDefinition {
    private String name;
    private String description;
    private Map<String, Object> parameters;
}
