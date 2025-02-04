//package com.springai.rag_pdf.Controller;
//
//import com.springai.rag_pdf.Service.DocumentManagementService;
//import com.springai.rag_pdf.Service.QuestionAnswerService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/documents")
//public class DocumentController {
//    private final DocumentManagementService documentService;
//    private final QuestionAnswerService qaService;
//
//    public DocumentController(DocumentManagementService documentService, QuestionAnswerService qaService) {
//        this.documentService = documentService;
//        this.qaService = qaService;
//    }
//
//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
//        try {
//            String documentId = documentService.uploadDocument(file);
//            return ResponseEntity.ok(documentId);
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().body("Failed to upload document: " + e.getMessage());
//        }
//    }
//
//    @GetMapping
//    public ResponseEntity<List<String>> listDocuments() {
//        return ResponseEntity.ok(documentService.listDocuments());
//    }
//
//    @DeleteMapping("/{documentId}")
//    public ResponseEntity<Void> deleteDocument(@PathVariable String documentId) {
//        documentService.deleteDocument(documentId);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/{documentId}/ask")
//    public ResponseEntity<String> askQuestion(
//            @PathVariable String documentId,
//            @RequestParam String question) {
//        try {
//            String answer = qaService.askQuestion(documentId, question);
//            return ResponseEntity.ok(answer);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.notFound().build();
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Error processing question: " + e.getMessage());
//        }
//    }
//}