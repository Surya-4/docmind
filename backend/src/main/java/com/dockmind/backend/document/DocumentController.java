package com.dockmind.backend.document;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dockmind.backend.dto.DocumentResponse;
import com.dockmind.backend.entity.DocumentStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

	private final DocumentService documentService;
	
	@PostMapping("/upload")
	public ResponseEntity<DocumentResponse> uploadDocument(@RequestParam("file") MultipartFile file,@AuthenticationPrincipal UserDetails principal){
		validateFile(file);
		DocumentResponse res=documentService.uploadDocument(file, principal.getUsername());
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(res);
	}
	
	@GetMapping("")
	public ResponseEntity<List<DocumentResponse>> getDocuments(@AuthenticationPrincipal UserDetails principal){
		List<DocumentResponse> res=documentService.getDocuments(principal.getUsername());
		return ResponseEntity.status(HttpStatus.OK).body(res);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable UUID id){
		DocumentResponse res=documentService.getDocumentById(id);
		return ResponseEntity.status(HttpStatus.OK).body(res);
	}
	
	@PatchMapping("/{id}/status")
	public void patchDocument(@PathVariable UUID id,@RequestParam("status") DocumentStatus status){
		documentService.patchDocument(id, status);
	}
	
	@DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable UUID id) {

        documentService.deleteDocumentById(id);
        return ResponseEntity.noContent().build();
    }
	
	private void validateFile(MultipartFile file) {
	    if (file.isEmpty()) {
	        throw new RuntimeException("File is empty");
	    }
	    if (!file.getContentType().equals("application/pdf")) {
	        throw new RuntimeException("Only PDF files are allowed");
	    }
	}
}
