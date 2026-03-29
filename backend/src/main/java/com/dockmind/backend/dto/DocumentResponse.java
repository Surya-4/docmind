package com.dockmind.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.dockmind.backend.entity.DocumentStatus;
import lombok.Data;

@Data
public class DocumentResponse {
	private UUID id; 	
	private String filename;
	private String filePath;
	private DocumentStatus status = DocumentStatus.PROCESSING;
    private LocalDateTime createdAt = LocalDateTime.now();
}
