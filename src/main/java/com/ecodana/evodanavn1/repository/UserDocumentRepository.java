package com.ecodana.evodanavn1.repository;

import com.ecodana.evodanavn1.model.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDocumentRepository extends JpaRepository<UserDocument, String> {

    List<UserDocument> findByUserId(String userId);

    Optional<UserDocument> findByUserIdAndDocumentType(String userId, String documentType);
}