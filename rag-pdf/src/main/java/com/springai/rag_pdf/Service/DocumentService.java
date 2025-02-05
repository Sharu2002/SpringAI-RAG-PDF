package com.springai.rag_pdf.Service;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    private final VectorStore vectorStore;

    public DocumentService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    // Approach 1: Retrieve documents by a dummy query that matches all documents
    public Set<String> getAllDocumentIds() {
        try {
            // Use a very broad query to retrieve all documents
            List<Document> allDocuments = vectorStore.similaritySearch("");

            // Extract unique document IDs from the metadata
            return allDocuments.stream()
                    .map(doc -> (String) doc.getMetadata().get("documentId"))
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());
        } catch (UnsupportedOperationException e) {
            // Fallback if similarity search doesn't work
            throw new RuntimeException("Unable to retrieve document IDs. Your VectorStore implementation may not support this operation.", e);
        }
    }

    // Alternative method for manually tracking document IDs
    // This requires modifying your upload process to maintain a separate tracking mechanism
    private Set<String> trackedDocumentIds = new HashSet<>();

    public void addTrackedDocumentId(String documentId) {
        trackedDocumentIds.add(documentId);
    }

    public Set<String> getTrackedDocumentIds() {
        return new HashSet<>(trackedDocumentIds);
    }
}