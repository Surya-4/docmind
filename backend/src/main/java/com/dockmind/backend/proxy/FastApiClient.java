package com.dockmind.backend.proxy;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.dockmind.backend.dto.QueryResponse;

@Component
public class FastApiClient {

	private final WebClient client;
	
	public FastApiClient(@Value("${fastapi.url}") String url) {
		this.client=WebClient.builder().baseUrl(url).build();
	}
	
	public void triggerIngestion(UUID documentId, String filePath) {
		client.post().uri("/ingest").
		bodyValue(Map.of(
				"documentId", documentId.toString(),
                "filePath", filePath)).
		retrieve().
		bodyToMono(Void.class).
		subscribe();
	}
	
	public QueryResponse query(UUID documentId, String question) {
	    return client.post()
	            .uri("/infer/query")
	            .bodyValue(Map.of(
	                    "documentId", documentId.toString(),
	                    "question", question
	            ))
	            .retrieve()
	            .bodyToMono(QueryResponse.class)
	            .block();
	}
}
