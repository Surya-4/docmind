package com.dockmind.backend.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import io.jsonwebtoken.io.IOException;

@Component
public class SupabaseStorageClient {
	private final WebClient client;
	private final String bucket;
	private final String supabaseKey;

	public SupabaseStorageClient(@Value("${supabase.url}") String supabaseUrl,
			@Value("${supabase.key}") String supabaseKey, @Value("${supabase.bucket}") String bucket) {

		this.supabaseKey = supabaseKey;
		this.bucket = bucket;
		this.client = WebClient.builder().baseUrl(supabaseUrl).build();
	}
	
	public String uploadFile(MultipartFile file) {
		try {
			String filename=System.currentTimeMillis() + "_" + file.getOriginalFilename();
			String uploadPath="/storage/v1/object/"+bucket+"/"+filename;
			byte[] bytes=file.getBytes();
			client.post().
				   uri(uploadPath).
				   header("Authorization" , "Bearer " + supabaseKey).
				   header("x-upsert", "true").
				   contentType(MediaType.APPLICATION_OCTET_STREAM).
				   bodyValue(bytes).
				   retrieve().
				   bodyToMono(Void.class).
				   block();
			
			return uploadPath;
		} catch (IOException e) {
			// TODO: handle exception
            throw new RuntimeException("Failed to upload file to Supabase: " + e.getMessage());
		}
		catch (Exception e) {
	        throw new RuntimeException("Failed to upload to Supabase: " + e.getMessage());
	    }
	}
}
