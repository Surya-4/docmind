package com.dockmind.backend.conversation;

import java.util.UUID;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dockmind.backend.dto.ConversationResponse;
import com.dockmind.backend.dto.MessageResponse;
import com.dockmind.backend.dto.QueryRequest;
import com.dockmind.backend.dto.QueryResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {
	
	private final ConversationService conversationService;
	
	@PostMapping
	public ResponseEntity<ConversationResponse> createConversation(@RequestParam UUID documentId ,@AuthenticationPrincipal UserDetails principal){
		ConversationResponse conversation=conversationService.createConversation(documentId, principal.getUsername());
		return ResponseEntity.status(201).body(conversation);
	}
	
	@GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        java.util.List<MessageResponse> res = conversationService
                .getMessages(id, userDetails.getUsername());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{id}/query")
    public ResponseEntity<QueryResponse> query(@PathVariable UUID id, @RequestBody QueryRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        QueryResponse res = conversationService
                .query(id, request.getQuestion(), userDetails.getUsername());
        return ResponseEntity.ok(res);
    }
}
