package com.dockmind.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ConversationResponse {
    private UUID id;
    private UUID documentId;
    private LocalDateTime createdAt;
}