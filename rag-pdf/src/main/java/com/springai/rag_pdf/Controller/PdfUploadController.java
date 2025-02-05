package com.springai.rag_pdf.Controller;


import com.springai.rag_pdf.Service.PdfFileReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class PdfUploadController {

    private final PdfFileReader pdfFileReader;

    public PdfUploadController(PdfFileReader pdfFileReader) {
        this.pdfFileReader = pdfFileReader;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file type
            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest().body("Only PDF files are allowed");
            }

            // Process the PDF file
            pdfFileReader.processPdfFile(file);

            return ResponseEntity.ok("PDF processed successfully");
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Error processing PDF: " + e.getMessage());
        }
    }

//    @PostMapping("/clear")
//    public ResponseEntity<String> clearVectorStore() {
//        try {
//            pdfFileReader.clearVectorStore();
//            return ResponseEntity.ok("Vector store cleared successfully");
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError()
//                    .body("Error clearing vector store: " + e.getMessage());
//        }
//    }
}
