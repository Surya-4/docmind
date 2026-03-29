package com.dockmind.backend.conversation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.dockmind.backend.auth.UserRepository;
import com.dockmind.backend.config.RedisClient;
import com.dockmind.backend.document.DocumentRepository;
import com.dockmind.backend.dto.ConversationResponse;
import com.dockmind.backend.dto.MessageResponse;
import com.dockmind.backend.dto.QueryResponse;
import com.dockmind.backend.entity.Conversation;
import com.dockmind.backend.entity.Document;
import com.dockmind.backend.entity.DocumentStatus;
import com.dockmind.backend.entity.Message;
import com.dockmind.backend.entity.MessageRole;
import com.dockmind.backend.entity.User;
import com.dockmind.backend.exceptions.ResourceNotFoundException;
import com.dockmind.backend.proxy.FastApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationService {

	private final ConversationRepository conversationRepository;
	private final MessageRepository messageRepository;
	private final UserRepository userRepository;
	private final DocumentRepository documentRepository;
	private final FastApiClient fastApiClient;
	private final RedisClient redisClient;
	private final ObjectMapper objectMapper;

	public ConversationResponse createConversation(UUID documentId, String email) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User does not exist"));
		Document doc = documentRepository.findById(documentId)
				.orElseThrow(() -> new ResourceNotFoundException("Document does not exist"));

		if (!doc.getUser().getId().equals(user.getId())) {
			throw new RuntimeException("Unauthorized — document does not belong to you");
		}

		if (doc.getStatus() != DocumentStatus.READY) {
			throw new RuntimeException("Document is still processing. Please wait.");
		}

		Conversation conversation = new Conversation();
		conversation.setDocument(doc);
		conversation.setUser(user);
		Conversation saved = conversationRepository.save(conversation);
		return toConversationDTO(saved);
	}

	public List<MessageResponse> getMessages(UUID conversationId, String email) {
		Conversation conversation = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

		if (!conversation.getUser().getEmail().equals(email)) {
			throw new RuntimeException("Unauthorized access to conversation");
		}
		List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
		List<MessageResponse> lst = new ArrayList<MessageResponse>();
		for (Message msg : messages) {
			lst.add(toMessageDTO(msg));
		}
		return lst;
	}

	public QueryResponse query(UUID conversationId, String question, String email) {
		Conversation conversation=conversationRepository.findById(conversationId).orElseThrow(()-> new RuntimeException("Conversation not found"));
		
		if(!conversation.getUser().getEmail().equals(email)) {
			throw new RuntimeException("Unauthorized access to conversation");
		}
		UUID documentId=conversation.getDocument().getId();
		String cacheKey = buildCacheKey(documentId, question);
		
		String cache=redisClient.get(cacheKey);
		if(cache!=null) {
			return deserialize(cache);
		}
		
		QueryResponse response=fastApiClient.query(documentId, question);
		
		saveMessage(conversation, MessageRole.USER, question);
		saveMessage(conversation,MessageRole.ASSISTANT, response.getAnswer());
		
        redisClient.set(cacheKey, serialize(response), 3600);

		return response;
	}

	private String buildCacheKey(UUID documentId, String question) {
		try {
			String raw = documentId.toString() + ":" + question;
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte b : hash) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException("Failed to build cache key");
		}
	}

	private String serialize(QueryResponse response) {
		try {
			return objectMapper.writeValueAsString(response);
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize response");
		}
	}

	private QueryResponse deserialize(String json) {
		try {
			return objectMapper.readValue(json, QueryResponse.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to deserialize cached response");
		}
	}

	private void saveMessage(Conversation conversation, MessageRole role, String content) {
		Message message = new Message();
		message.setConversation(conversation);
		message.setRole(role);
		message.setContent(content);
		messageRepository.save(message);
	}

	private ConversationResponse toConversationDTO(Conversation conversation) {
		ConversationResponse response = new ConversationResponse();
		response.setId(conversation.getId());
		response.setDocumentId(conversation.getDocument().getId());
		response.setCreatedAt(conversation.getCreatedAt());
		return response;
	}

	private MessageResponse toMessageDTO(Message message) {
		MessageResponse dto = new MessageResponse();
		BeanUtils.copyProperties(message, dto);
		dto.setRole(message.getRole().name());
		return dto;
	}
}
