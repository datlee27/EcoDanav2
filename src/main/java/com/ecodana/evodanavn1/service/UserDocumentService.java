package com.ecodana.evodanavn1.service;

import com.ecodana.evodanavn1.model.UserDocument;
import com.ecodana.evodanavn1.repository.UserDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserDocumentService {

    @Autowired
    private UserDocumentRepository userDocumentRepository;

    public List<UserDocument> getDocumentsByUserId(String userId) {
        return userDocumentRepository.findByUserId(userId);
    }

    public Optional<UserDocument> getDocumentByUserIdAndType(String userId, String docType) {
        return userDocumentRepository.findByUserIdAndDocumentType(userId, docType);
    }

    public UserDocument saveDocument(UserDocument document) {
        return userDocumentRepository.save(document);
    }

    public void deleteDocument(String documentId) {
        userDocumentRepository.deleteById(documentId);
    }
}