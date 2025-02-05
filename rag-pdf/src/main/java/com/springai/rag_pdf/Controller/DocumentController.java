package com.springai.rag_pdf.Controller;

import com.springai.rag_pdf.Service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/documents")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/ids")
    public ResponseEntity<Set<String>> listDocumentIds() {
        try {
            Set<String> documentIds = documentService.getAllDocumentIds();
            return ResponseEntity.ok(documentIds);
        } catch (RuntimeException e) {
            // Fallback to tracked document IDs if retrieval fails
            Set<String> trackedIds = documentService.getTrackedDocumentIds();
            return ResponseEntity.ok(trackedIds);
        }
    }
}