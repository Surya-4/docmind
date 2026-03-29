package com.dockmind.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageResponse {
    private UUID id;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}