package com.personalassistant.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiServiceImpl implements AIService {

    private final RuleBasedFallbackAIService fallbackService;
    private final AIToolDispatcher toolDispatcher;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:gpt-4o-mini}")
    private String model;

    @Value("${app.ai.api-url:https://api.openai.com/v1}")
    private String apiUrl;

    @Override
    public AIResponse chat(AIRequest request) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.info("No AI_API_KEY configured. Utilizing local deterministic AI intent engine.");
            return fallbackService.processIntent(request);
        }

        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(apiUrl)
                    .defaultHeader("Authorization", "Bearer " + apiKey.trim())
                    .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .build();

            List<Map<String, Object>> messages = new ArrayList<>();
            // System prompt
            messages.add(Map.of("role", "system", "content", request.getSystemContext()));

            // Conversation history
            if (request.getHistory() != null) {
                for (ChatMessageDto msg : request.getHistory()) {
                    messages.add(Map.of("role", msg.getRole().name().toLowerCase(), "content", msg.getContent()));
                }
            }

            // Current user prompt
            messages.add(Map.of("role", "user", "content", request.getPrompt()));

            // Build tools schema list for OpenAI
            List<Map<String, Object>> toolsList = new ArrayList<>();
            if (request.getTools() != null) {
                for (AIToolDefinition def : request.getTools()) {
                    toolsList.add(Map.of(
                            "type", "function",
                            "function", Map.of(
                                    "name", def.getName(),
                                    "description", def.getDescription(),
                                    "parameters", def.getParameters()
                            )
                    ));
                }
            }

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            if (!toolsList.isEmpty()) {
                body.put("tools", toolsList);
                body.put("tool_choice", "auto");
            }

            String responseJson = restClient.post()
                    .uri("/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode choice = root.path("choices").path(0).path("message");

            List<AIToolCall> toolCalls = new ArrayList<>();
            List<ToolResult> toolResults = new ArrayList<>();
            String assistantReply = choice.path("content").asText(null);

            if (choice.has("tool_calls")) {
                for (JsonNode tcNode : choice.path("tool_calls")) {
                    String toolId = tcNode.path("id").asText();
                    String toolName = tcNode.path("function").path("name").asText();
                    String argsJson = tcNode.path("function").path("arguments").asText();

                    Map<String, Object> args = objectMapper.readValue(argsJson, Map.class);

                    AIToolCall tc = AIToolCall.builder()
                            .id(toolId)
                            .toolName(toolName)
                            .arguments(args)
                            .build();
                    toolCalls.add(tc);

                    ToolResult result = toolDispatcher.executeTool(request.getUserId(), toolName, args);
                    toolResults.add(result);
                }
            }

            if (assistantReply == null || assistantReply.isBlank()) {
                if (!toolResults.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (ToolResult tr : toolResults) {
                        sb.append(tr.getMessage()).append("\n");
                    }
                    assistantReply = sb.toString().trim();
                } else {
                    assistantReply = "Action processed.";
                }
            }

            return AIResponse.builder()
                    .reply(assistantReply)
                    .toolCalls(toolCalls)
                    .toolResults(toolResults)
                    .build();

        } catch (Exception ex) {
            log.warn("External AI call failed or timeout: {}. Falling back to rule engine.", ex.getMessage());
            return fallbackService.processIntent(request);
        }
    }
}
