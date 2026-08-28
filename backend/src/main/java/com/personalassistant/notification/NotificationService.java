package com.personalassistant.notification;

import com.personalassistant.common.PagedResponse;
import com.personalassistant.common.ResourceNotFoundException;
import com.personalassistant.user.User;
import com.personalassistant.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @Transactional
    public NotificationResponse createNotification(User user, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getRecentNotifications(String userId) {
        User user = userService.getUserById(userId);
        return notificationRepository.findTop20ByUserOrderByCreatedAtDesc(user).stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getNotifications(String userId, boolean unreadOnly, int page, int size) {
        User user = userService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> notificationPage = unreadOnly
                ? notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user, pageable)
                : notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        List<NotificationResponse> content = notificationPage.getContent().stream()
                .map(NotificationResponse::fromEntity)
                .toList();

        return PagedResponse.<NotificationResponse>builder()
                .content(content)
                .page(notificationPage.getNumber())
                .size(notificationPage.getSize())
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .last(notificationPage.isLast())
                .build();
    }

    @Transactional
    public NotificationResponse markAsRead(String userId, String notificationId) {
        User user = userService.getUserById(userId);
        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(updated);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        User user = userService.getUserById(userId);
        notificationRepository.markAllAsReadForUser(user);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        User user = userService.getUserById(userId);
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
}
