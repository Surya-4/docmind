package com.dockmind.backend.document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.dockmind.backend.auth.UserRepository;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dockmind.backend.entity.Document;
import com.dockmind.backend.entity.DocumentStatus;
import com.dockmind.backend.entity.User;
import com.dockmind.backend.exceptions.ResourceNotFoundException;
import com.dockmind.backend.proxy.FastApiClient;
import com.dockmind.backend.proxy.SupabaseStorageClient;
import com.dockmind.backend.dto.DocumentResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {
	private final DocumentRepository documentRepository;
	private final UserRepository userRepository;
	private final SupabaseStorageClient storageClient;
	private final FastApiClient fastApiClient;
	public DocumentResponse uploadDocument(MultipartFile file,String email) {
		User user = userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User does not exist"));
		String filepath=storageClient.uploadFile(file);
		Document doc=new Document();
		doc.setFilename(file.getOriginalFilename());
		doc.setStatus(DocumentStatus.PROCESSING);
		doc.setUser(user);
		doc.setFilePath(filepath);
		Document saved=documentRepository.save(doc);
		
		fastApiClient.triggerIngestion(saved.getId(), filepath);
		
		return toDTO(saved);
	}
	
	public List<DocumentResponse> getDocuments(String email){
		User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Document> lst= documentRepository.findByUser(user);
        List<DocumentResponse> res=new ArrayList<DocumentResponse>();
        for(Document doc:lst) {
        	res.add(toDTO(doc));
        }
        return res;
	}
	
	public DocumentResponse getDocumentById(UUID id){
		Document document = documentRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Document not found"));
        return toDTO(document);
	}
	
	public void deleteDocumentById(UUID id) {
        documentRepository.deleteById(id);
    }
	
	public void patchDocument(UUID id,DocumentStatus status) {
		 Document document = documentRepository.findById(id)
	                .orElseThrow(() -> new RuntimeException("Document not found"));
	     document.setStatus(status);
	     documentRepository.save(document);
	}
	
	public DocumentResponse toDTO(Document document) {
        DocumentResponse dto = new DocumentResponse();
        BeanUtils.copyProperties(document, dto);
        return dto;
    }
	
	
}
