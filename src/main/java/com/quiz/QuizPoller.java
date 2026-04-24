package com.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class QuizPoller {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    private final String regNo;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public QuizPoller(String regNo, String baseUrl) {
        this.regNo = regNo;
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    public PollResponse poll(int pollIndex) {
        String url = baseUrl + "/quiz/messages?regNo=" + regNo + "&poll=" + pollIndex;
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                System.out.printf("  [Poll %d] Attempt %d/%d — GET %s%n",
                        pollIndex, attempt, MAX_RETRIES, url);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(15))
                        .GET()
                        .build();

                HttpResponse<String> response =
                        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("HTTP " + response.statusCode() + " | " + response.body());
                }

                PollResponse parsed = mapper.readValue(response.body(), PollResponse.class);
                int count = parsed.getEvents() == null ? 0 : parsed.getEvents().size();
                System.out.printf("  [Poll %d] OK — %d event(s) received%n", pollIndex, count);
                return parsed;

            } catch (Exception e) {
                lastException = e;
                System.out.printf("  [Poll %d] Attempt %d failed: %s%n", pollIndex, attempt, e.getMessage());

                if (attempt < MAX_RETRIES) {
                    System.out.printf("  [Poll %d] Retrying in %dms...%n", pollIndex, RETRY_DELAY_MS);
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry delay", ie);
                    }
                }
            }
        }

        throw new RuntimeException("Poll " + pollIndex + " failed after " + MAX_RETRIES + " attempts", lastException);
    }
}
