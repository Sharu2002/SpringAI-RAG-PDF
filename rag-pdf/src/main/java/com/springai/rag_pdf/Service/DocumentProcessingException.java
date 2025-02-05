package com.springai.rag_pdf.Service;

/**
 * Custom exception for document processing errors
 */
public class DocumentProcessingException extends RuntimeException {
    public DocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
