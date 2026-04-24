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
    private final HttpClient client;
    private final ObjectMapper mapper;

    public QuizSubmitter(String regNo) {
        this.regNo = regNo;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
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

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        System.out.println("\nSubmitting leaderboard...");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://devapigw.vidalhealthtpa.com/srm-quiz-task/quiz/submit"))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response Status: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
    }
}
