package com.vanshika.pdf_summarizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication//this tells Spring Boot that this is the main class for the application, and it should start the application when this class is run.
public class PdfSummarizerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PdfSummarizerApplication.class, args);//this line starts the Spring Boot application by calling the run method of the SpringApplication class, passing in the current class and any command-line arguments.
	}
}
