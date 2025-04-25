package com.example.bajajaq1.component;

import com.example.bajajaq1.model.WebhookResponse;
import com.example.bajajaq1.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting webhook process...");
        
        // Generate webhook and get response
        WebhookResponse response = webhookService.generateWebhook();
        System.out.println("\nReceived webhook response:");
        System.out.println("Webhook URL: " + response.getWebhook());
        System.out.println("Access Token: " + response.getAccessToken());
        System.out.println("\nComplete Response Data:");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response.getData()));
        
        // Solve the problem
        var result = webhookService.solveProblem(response);
        
        // Print the result before sending
        System.out.println("\nResult to be sent:");
        String jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        System.out.println(jsonResult);
        
        // Send the result to the webhook
        System.out.println("\nSending result to webhook...");
        webhookService.sendResult(response.getWebhook(), response.getAccessToken(), result);
        System.out.println("Result sent successfully!");
    }
} 