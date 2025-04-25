package com.example.bajajaq1.model;

import lombok.Data;
import java.util.List;

@Data
public class WebhookResponse {
    private String webhook;
    private String accessToken;
    private ResponseData data;

    @Data
    public static class ResponseData {
        private String regNo;
        private List<User> users;
        private Integer findId;  // For nth level followers
        private Integer n;       // For nth level followers
    }

    @Data
    public static class User {
        private int id;
        private String name;
        private List<Integer> follows;
    }
} 