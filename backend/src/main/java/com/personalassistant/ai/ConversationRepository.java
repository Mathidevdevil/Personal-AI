package com.personalassistant.ai;

import com.personalassistant.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    List<Conversation> findByUserOrderByUpdatedAtDesc(User user);

    Page<Conversation> findByUserOrderByUpdatedAtDesc(User user, Pageable pageable);

    Optional<Conversation> findByIdAndUser(String id, User user);
}
