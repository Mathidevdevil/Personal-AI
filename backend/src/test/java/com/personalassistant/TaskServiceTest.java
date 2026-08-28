package com.personalassistant;

import com.personalassistant.task.*;
import com.personalassistant.user.User;
import com.personalassistant.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = userRepository.findByEmail("demo@personalai.com").orElseThrow();
    }

    @Test
    void testTaskLifecycle() {
        TaskRequest req = TaskRequest.builder()
                .title("Complete API Unit Tests")
                .description("Write tests for all controller endpoints")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .dueDate(Instant.now().plus(2, ChronoUnit.DAYS))
                .build();

        TaskResponse created = taskService.createTask(testUser.getId(), req);
        assertNotNull(created.getId());
        assertEquals(TaskStatus.TODO, created.getStatus());

        // Toggle complete
        TaskResponse completed = taskService.toggleComplete(testUser.getId(), created.getId());
        assertEquals(TaskStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getCompletedAt());
    }
}
