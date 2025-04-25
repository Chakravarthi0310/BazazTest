package com.example.bajajaq1.service;

import com.example.bajajaq1.model.WebhookRequest;
import com.example.bajajaq1.model.WebhookResponse;
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
        // Extract the last digit of regNo to determine which problem to solve
        String regNo = response.getData().getRegNo();
        int lastDigit = Character.getNumericValue(regNo.charAt(regNo.length() - 1));
        
        Map<String, Object> result = new HashMap<>();
        result.put("regNo", regNo);
        
        if (lastDigit % 2 == 0) {
            // Even number - solve nth level followers
            List<Integer> nthLevelFollowers = findNthLevelFollowers(
                response.getData().getUsers(),
                response.getData().getFindId(),
                response.getData().getN()
            );
            result.put("outcome", nthLevelFollowers);
        } else {
            // Odd number - solve mutual followers
            List<List<Integer>> mutualFollowers = findMutualFollowers(response.getData().getUsers());
            result.put("outcome", mutualFollowers);
        }
        
        return result;
    }

    private List<List<Integer>> findMutualFollowers(List<WebhookResponse.User> users) {
        List<List<Integer>> result = new ArrayList<>();
        Set<String> processedPairs = new HashSet<>();

        for (WebhookResponse.User user : users) {
            for (Integer followedId : user.getFollows()) {
                WebhookResponse.User followedUser = users.stream()
                        .filter(u -> u.getId() == followedId)
                        .findFirst()
                        .orElse(null);

                if (followedUser != null && followedUser.getFollows().contains(user.getId())) {
                    int min = Math.min(user.getId(), followedId);
                    int max = Math.max(user.getId(), followedId);
                    String pairKey = min + "," + max;

                    if (!processedPairs.contains(pairKey)) {
                        processedPairs.add(pairKey);
                        result.add(Arrays.asList(min, max));
                    }
                }
            }
        }

        return result;
    }

    private List<Integer> findNthLevelFollowers(List<WebhookResponse.User> users, int findId, int n) {
        if (n <= 0) {
            return Collections.emptyList();
        }

        Set<Integer> currentLevel = new HashSet<>();
        Set<Integer> visited = new HashSet<>();
        currentLevel.add(findId);
        visited.add(findId);

        for (int level = 1; level <= n; level++) {
            Set<Integer> nextLevel = new HashSet<>();
            
            for (int userId : currentLevel) {
                WebhookResponse.User user = users.stream()
                        .filter(u -> u.getId() == userId)
                        .findFirst()
                        .orElse(null);

                if (user != null) {
                    for (int followedId : user.getFollows()) {
                        if (!visited.contains(followedId)) {
                            nextLevel.add(followedId);
                            visited.add(followedId);
                        }
                    }
                }
            }

            if (level == n) {
                return new ArrayList<>(nextLevel);
            }

            currentLevel = nextLevel;
            if (currentLevel.isEmpty()) {
                break;
            }
        }

        return Collections.emptyList();
    }
} 