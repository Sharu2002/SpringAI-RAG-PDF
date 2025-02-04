//package com.springai.rag_pdf.Service;
//
//import org.springframework.ai.document.Document;
//import org.springframework.ai.embedding.EmbeddingClient;
//import org.springframework.ai.reader.ExtractedTextFormatter;
//import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
//import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
//import org.springframework.ai.transformer.splitter.TokenTextSplitter;
//import org.springframework.ai.vectorstore.PgVectorStore;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import jakarta.annotation.PostConstruct;
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//public class DocumentManagementService {
//    private final Map<String, VectorStore> documentVectorStores;
//    private final String uploadDir = "uploaded-documents";
//    private final PdfDocumentReaderConfig defaultConfig;
//    private final TokenTextSplitter textSplitter;
//    private final JdbcTemplate jdbcTemplate;
//    private final EmbeddingClient embeddingClient;
//
//    @Autowired
//    public DocumentManagementService(JdbcTemplate jdbcTemplate, EmbeddingClient embeddingClient) {
//        this.jdbcTemplate = jdbcTemplate;
//        this.embeddingClient = embeddingClient;
//        this.documentVectorStores = new HashMap<>();
//        this.textSplitter = new TokenTextSplitter();
//        this.defaultConfig = PdfDocumentReaderConfig.builder()
//                .withPageExtractedTextFormatter(
//                        new ExtractedTextFormatter.Builder()
//                                .withPagePrefix("Page ")
//                                .withPageSeparator("\n\n")
//                                .build())
//                .build();
//
//        // Create upload directory if it doesn't exist
//        try {
//            Files.createDirectories(Paths.get(uploadDir));
//        } catch (IOException e) {
//            throw new RuntimeException("Could not create upload directory", e);
//        }
//    }
//
//    @PostConstruct
//    public void init() {
//        // Create the vector store table if it doesn't exist
//        createVectorStoreTable();
//    }
//
//    private void createVectorStoreTable() {
//        jdbcTemplate.execute("""
//            CREATE TABLE IF NOT EXISTS vector_store (
//                id UUID PRIMARY KEY,
//                document_id VARCHAR(255) NOT NULL,
//                content TEXT,
//                metadata JSONB,
//                embedding VECTOR(1536)
//            )
//        """);
//
//        // Create an index for faster similarity search
//        jdbcTemplate.execute("""
//            CREATE INDEX IF NOT EXISTS vector_store_embedding_idx
//            ON vector_store
//            USING ivfflat (embedding vector_cosine_ops)
//        """);
//    }
//
//    public String uploadDocument(MultipartFile file) throws IOException {
//        // Generate unique ID for the document
//        String documentId = UUID.randomUUID().toString();
//
//        // Save the file
//        String fileName = documentId + "_" + file.getOriginalFilename();
//        Path filePath = Paths.get(uploadDir, fileName);
//        file.transferTo(filePath.toFile());
//
//        // Create a new vector store for this document
//        VectorStore documentVectorStore = createVectorStore(documentId);
//
//        // Process the PDF and store its vectors
//        processPdfDocument(filePath.toFile(), documentVectorStore, documentId);
//
//        // Store the vector store reference
//        documentVectorStores.put(documentId, documentVectorStore);
//
//        return documentId;
//    }
//
//    private VectorStore createVectorStore(String documentId) {
//        return new PgVectorStore(jdbcTemplate, embeddingClient,
//                PgVectorStore.PgVectorStoreConfig.builder()
//                        .withTableName("vector_store")
//                        .withFilterMetadata("document_id = '" + documentId + "'")
//                        .build());
//    }
//
//    private void processPdfDocument(File pdfFile, VectorStore vectorStore, String documentId) throws IOException {
//        var pdfReader = new PagePdfDocumentReader(pdfFile, defaultConfig);
//        List<Document> documents = textSplitter.apply(pdfReader.get());
//
//        // Add document ID to metadata
//        documents.forEach(doc -> {
//            Map<String, String> metadata = new HashMap<>(doc.getMetadata());
//            metadata.put("document_id", documentId);
//            doc.setMetadata(metadata);
//        });
//
//        vectorStore.accept(documents);
//    }
//
//    public VectorStore getDocumentVectorStore(String documentId) {
//        VectorStore vectorStore = documentVectorStores.get(documentId);
//        if (vectorStore == null) {
//            // Try to recreate the vector store if it exists in the database
//            if (documentExists(documentId)) {
//                vectorStore = createVectorStore(documentId);
//                documentVectorStores.put(documentId, vectorStore);
//            } else {
//                throw new IllegalArgumentException("Document not found: " + documentId);
//            }
//        }
//        return vectorStore;
//    }
//
//    private boolean documentExists(String documentId) {
//        Integer count = jdbcTemplate.queryForObject(
//                "SELECT COUNT(*) FROM vector_store WHERE document_id = ?",
//                Integer.class,
//                documentId
//        );
//        return count != null && count > 0;
//    }
//
//    public List<String> listDocuments() {
//        return jdbcTemplate.queryForList(
//                "SELECT DISTINCT document_id FROM vector_store",
//                String.class
//        );
//    }
//
//    public void deleteDocument(String documentId) {
//        // Remove from memory
//        documentVectorStores.remove(documentId);
//
//        // Delete from database
//        jdbcTemplate.update(
//                "DELETE FROM vector_store WHERE document_id = ?",
//                documentId
//        );
//
//        // Delete physical files
//        try {
//            Files.list(Paths.get(uploadDir))
//                    .filter(path -> path.getFileName().toString().startsWith(documentId))
//                    .forEach(path -> {
//                        try {
//                            Files.delete(path);
//                        } catch (IOException e) {
//                            throw new RuntimeException("Failed to delete file: " + path, e);
//                        }
//                    });
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to clean up document files", e);
//        }
//    }
//
//    public int getDocumentCount() {
//        Integer count = jdbcTemplate.queryForObject(
//                "SELECT COUNT(DISTINCT document_id) FROM vector_store",
//                Integer.class
//        );
//        return count != null ? count : 0;
//    }
//
//    public Map<String, Integer> getDocumentStatistics(String documentId) {
//        Integer chunkCount = jdbcTemplate.queryForObject(
//                "SELECT COUNT(*) FROM vector_store WHERE document_id = ?",
//                Integer.class,
//                documentId
//        );
//
//        Map<String, Integer> stats = new HashMap<>();
//        stats.put("totalChunks", chunkCount != null ? chunkCount : 0);
//        return stats;
//    }
//}