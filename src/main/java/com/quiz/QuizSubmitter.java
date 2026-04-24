package com.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class QuizSubmitter {

    private final String regNo;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public QuizSubmitter(String regNo, String baseUrl) {
        this.regNo = regNo;
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    public void submit(List<LeaderboardEntry> leaderboard) throws Exception {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("regNo", regNo);

        ArrayNode lb = payload.putArray("leaderboard");
        for (LeaderboardEntry entry : leaderboard) {
            ObjectNode node = lb.addObject();
            node.put("participant", entry.getParticipant());
            node.put("totalScore", entry.getTotalScore());
        }

        String body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);

        System.out.println("\n========== SUBMITTING LEADERBOARD ==========");
        System.out.println("Payload:\n" + body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/quiz/submit"))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("\n--- Submit Response (HTTP " + response.statusCode() + ") ---");
        System.out.println(response.body());

        try {
            JsonNode result = mapper.readTree(response.body());
            System.out.println("\n========== RESULT ==========");
            System.out.println("isCorrect     : " + result.path("isCorrect").asText());
            System.out.println("isIdempotent  : " + result.path("isIdempotent").asText());
            System.out.println("submittedTotal: " + result.path("submittedTotal").asText());
            System.out.println("expectedTotal : " + result.path("expectedTotal").asText());
            System.out.println("message       : " + result.path("message").asText());
        } catch (Exception e) {
            System.out.println("(Could not parse response as JSON)");
        }
    }
}
