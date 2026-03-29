package com.dockmind.backend.document;
import com.dockmind.backend.entity.Document;
import com.dockmind.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document , UUID>{

	List<Document> findByUser(User user);

}
