package com.springai.rag_pdf;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class RagPdfApplication {


	public static void main(String[] args) {

		System.setProperty("spring.profiles.active", "llama");

		SpringApplication.run(RagPdfApplication.class, args);
	}

	@Configuration
	@Profile("llama")
	class AppConfig {
		@Bean
		VectorStore vectorStore(EmbeddingClient embeddingClient) {
			return new SimpleVectorStore(embeddingClient);
		}

	}
}
