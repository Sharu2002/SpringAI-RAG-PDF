package com.springai.rag_pdf.Service;

import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

@Component
public class PdfFileReader {
    private final VectorStore vectorStore;
    private final DocumentService documentService;  // Add DocumentService dependency

    public PdfFileReader(VectorStore vectorStore, DocumentService documentService) {
        this.vectorStore = vectorStore;
        this.documentService = documentService;
    }

    public void processPdfFile(MultipartFile file) throws IOException {
        // Generate a document ID using the original filename
        String documentId = generateDocumentId(file.getOriginalFilename());

        // Create a temporary file to store the uploaded PDF
        Path tempFile = Files.createTempFile("uploaded_pdf_", ".pdf");
        file.transferTo(tempFile.toFile());

        // Create a FileSystemResource from the temporary file
        Resource pdfResource = new FileSystemResource(tempFile.toFile());

        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(
                        new ExtractedTextFormatter.Builder()
                                .build())
                .build();

        var pdfReader = new PagePdfDocumentReader(pdfResource, config);
        var textSplitter = new TokenTextSplitter();

        try {
            vectorStore.accept(textSplitter.apply(pdfReader.get()));
            // Track the document ID after successful processing
            documentService.addTrackedDocumentId(documentId);
        } finally {
            // Clean up the temporary file
            Files.deleteIfExists(tempFile);
        }
    }

    private String generateDocumentId(String originalFilename) {
        // Remove file extension and sanitize the filename
        String baseName = originalFilename != null ?
                originalFilename.replaceFirst("[.][^.]+$", "") :
                "unnamed_document";

        // Add timestamp to ensure uniqueness
        return baseName + "_" + System.currentTimeMillis();
    }
}