package com.personalassistant;

import com.personalassistant.ai.AIChatRequest;
import com.personalassistant.ai.AIChatResponse;
import com.personalassistant.ai.ConversationService;
import com.personalassistant.user.User;
import com.personalassistant.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AIServiceToolExecutionTest {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = userRepository.findByEmail("demo@personalai.com").orElseThrow();
    }

    @Test
    void testAIChatToolCallingExpense() {
        AIChatRequest req = AIChatRequest.builder()
                .message("Add ₹500 for dinner")
                .build();

        AIChatResponse res = conversationService.handleUserChat(testUser.getId(), req);
        assertNotNull(res.getConversationId());
        assertNotNull(res.getMessage());
        assertFalse(res.getToolResults().isEmpty());
        assertEquals("createExpense", res.getToolResults().get(0).getToolName());
        assertTrue(res.getToolResults().get(0).isSuccess());
    }

    @Test
    void testAIChatToolCallingTask() {
        AIChatRequest req = AIChatRequest.builder()
                .message("Create a task to finish my resume tomorrow")
                .build();

        AIChatResponse res = conversationService.handleUserChat(testUser.getId(), req);
        assertNotNull(res.getConversationId());
        assertFalse(res.getToolResults().isEmpty());
        assertEquals("createTask", res.getToolResults().get(0).getToolName());
        assertTrue(res.getToolResults().get(0).isSuccess());
    }

    @Test
    void testAIChatToolCallingTripPlan() {
        AIChatRequest req = AIChatRequest.builder()
                .message("Plan a 3-day Bangalore trip under ₹15,000")
                .build();

        AIChatResponse res = conversationService.handleUserChat(testUser.getId(), req);
        assertNotNull(res.getConversationId());
        assertFalse(res.getToolResults().isEmpty());
        assertEquals("generateTripPlan", res.getToolResults().get(0).getToolName());
        assertTrue(res.getToolResults().get(0).isSuccess());
    }
}
