package com.example.bajajaq1.component;

import com.example.bajajaq1.model.WebhookResponse;
import com.example.bajajaq1.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private WebhookService webhookService;

    @Override
    public void run(String... args) throws Exception {
        // Generate webhook and get response
        WebhookResponse response = webhookService.generateWebhook();
        
        // Solve the problem
        var result = webhookService.solveProblem(response);
        
        // Send the result to the webhook
        webhookService.sendResult(response.getWebhook(), response.getAccessToken(), result);
    }
} 