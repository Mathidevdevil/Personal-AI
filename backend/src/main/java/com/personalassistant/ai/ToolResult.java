package com.personalassistant.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {
    private String toolName;
    private boolean success;
    private String message;
    private Object data;
    private String module;

    public static ToolResult success(String toolName, String module, String message, Object data) {
        return ToolResult.builder()
                .toolName(toolName)
                .module(module)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static ToolResult error(String toolName, String module, String message) {
        return ToolResult.builder()
                .toolName(toolName)
                .module(module)
                .success(false)
                .message(message)
                .build();
    }
}
