package com.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class QuizPoller {
    private final String regNo;
    private final HttpClient client;
    private final ObjectMapper mapper;

    public QuizPoller(String regNo) {
        this.regNo = regNo;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.mapper = new ObjectMapper();
    }

    public PollResponse poll(int index) throws Exception {
        String url = "https://devapigw.vidalhealthtpa.com/srm-quiz-task/quiz/messages?regNo=" + regNo + "&poll=" + index;
        
        for (int i = 1; i <= 3; i++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(15))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    PollResponse res = mapper.readValue(response.body(), PollResponse.class);
                    System.out.println("Poll " + index + " success. Events: " + (res.getEvents() != null ? res.getEvents().size() : 0));
                    return res;
                }
                System.out.println("Attempt " + i + " failed with status " + response.statusCode());
            } catch (Exception e) {
                System.out.println("Attempt " + i + " error: " + e.getMessage());
            }

            if (i < 3) Thread.sleep(2000);
        }
        throw new Exception("Failed to poll index " + index + " after 3 attempts.");
    }
}
