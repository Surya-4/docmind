package com.dockmind.backend.conversation;

import com.dockmind.backend.entity.Conversation;
import com.dockmind.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ConversationRepository 
        extends JpaRepository<Conversation, UUID> {

    List<Conversation> findByUser(User user);
    List<Conversation> findByDocumentId(UUID documentId);
}