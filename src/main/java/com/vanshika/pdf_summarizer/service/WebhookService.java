package com.vanshika.pdf_summarizer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {

    // reads n8n webhook URL from application.properties
    @Value("${n8n.webhook-url}")//this tells Spring to read the value of the n8n.webhook-url property from the application.properties file and assign it to the webhookUrl variable. This allows you to configure the webhook URL without hardcoding it in the code.
    private String webhookUrl;

    // RestTemplate is Java's tool for making HTTP requests
    // like axios in Node.js
    private RestTemplate restTemplate = new RestTemplate();

    @Async
    public void triggerN8n(String pdfKey, String summaryKey) {
        try {
            // Step 1 - set headers
            // telling n8n we are sending JSON data
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Step 2 - create the message body
            // this is what we send to n8n
            Map<String, String> body = new HashMap<>();//
            body.put("bucket", "project-pdf-summarizer");
            body.put("pdfKey", pdfKey);
            body.put("summaryKey", summaryKey);

            // Step 3 - combine headers and body
            HttpEntity<Map<String, String>> request =
                new HttpEntity<>(body, headers);

            // Step 4 - send POST request to n8n
            restTemplate.postForObject(webhookUrl, request, String.class);

            System.out.println("✅ n8n triggered successfully!");

        } catch (Exception e) {
            System.out.println("❌ n8n webhook failed: " + e.getMessage());
            throw new RuntimeException("Webhook failed: " + e.getMessage());
        }
    }
}
