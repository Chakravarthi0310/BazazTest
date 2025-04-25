package com.example.bajajaq1.service;

import com.example.bajajaq1.model.WebhookRequest;
import com.example.bajajaq1.model.WebhookResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WebhookService {

    private static final String GENERATE_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public WebhookResponse generateWebhook() {
        WebhookRequest request = new WebhookRequest();
        request.setName("Padyala Chakravarthi");
        request.setRegNo("AP22110011269");
        request.setEmail("chakravarthi_padyala@srmap.edu.in");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<WebhookRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<WebhookResponse> response = restTemplate.exchange(
                GENERATE_WEBHOOK_URL,
                HttpMethod.POST,
                entity,
                WebhookResponse.class
        );

        return response.getBody();
    }

    @Retryable(maxAttempts = 4, backoff = @Backoff(delay = 1000))
    public void sendResult(String webhookUrl, String accessToken, Map<String, Object> result) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(result, headers);
        restTemplate.exchange(
                webhookUrl,
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    public Map<String, Object> solveProblem(WebhookResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        // Get the users data from the nested structure
        Map<String, Object> usersData = (Map<String, Object>) response.getData().get("users");
        if (usersData == null) {
            System.out.println("No users data found in response");
            return result;
        }

        List<WebhookResponse.User> users = convertToUsers(usersData.get("users"));
        System.out.println("\nProcessing " + users.size() + " users to find mutual followers...");

        // Find mutual followers
        List<List<Integer>> mutualFollowers = findMutualFollowers(users);
        System.out.println("Found " + mutualFollowers.size() + " mutual follower pairs");

        // Prepare the result
        result.put("regNo", "AP22110011269");  // Using the registration number from the request
        result.put("outcome", mutualFollowers);
        
        return result;
    }

    private List<WebhookResponse.User> convertToUsers(Object usersData) {
        try {
            return objectMapper.convertValue(usersData, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, WebhookResponse.User.class));
        } catch (Exception e) {
            System.out.println("Error converting users data: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<List<Integer>> findMutualFollowers(List<WebhookResponse.User> users) {
        List<List<Integer>> result = new ArrayList<>();
        Set<String> processedPairs = new HashSet<>();

        for (WebhookResponse.User user : users) {
            System.out.println("\nProcessing user " + user.getId() + " (" + user.getName() + ") who follows: " + user.getFollows());
            for (Integer followedId : user.getFollows()) {
                WebhookResponse.User followedUser = users.stream()
                        .filter(u -> u.getId() == followedId)
                        .findFirst()
                        .orElse(null);

                if (followedUser != null) {
                    System.out.println("  Checking if user " + followedId + " (" + followedUser.getName() + ") follows back...");
                    System.out.println("  User " + followedId + "'s follows list: " + followedUser.getFollows());
                    
                    if (followedUser.getFollows().contains(user.getId())) {
                        int min = Math.min(user.getId(), followedId);
                        int max = Math.max(user.getId(), followedId);
                        String pairKey = min + "," + max;

                        if (!processedPairs.contains(pairKey)) {
                            processedPairs.add(pairKey);
                            result.add(Arrays.asList(min, max));
                            System.out.println("  ✓ Found mutual followers: " + min + " and " + max);
                        }
                    } else {
                        System.out.println("  ✗ No mutual follow");
                    }
                }
            }
        }

        return result;
    }
} 