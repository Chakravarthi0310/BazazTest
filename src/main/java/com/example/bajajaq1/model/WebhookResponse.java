package com.example.bajajaq1.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class WebhookResponse {
    private String webhook;
    private String accessToken;
    private Map<String, Object> data;

    @Data
    public static class User {
        private int id;
        private String name;
        private List<Integer> follows;
    }
} 