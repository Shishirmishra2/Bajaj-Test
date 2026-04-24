package com.quiz;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            config.load(fis);
        } catch (IOException e) {
            System.err.println("Could not load config.properties. Ensure it exists.");
            System.exit(1);
        }

        String regNo = config.getProperty("reg.no");
        String baseUrl = config.getProperty("api.base.url");
        int totalPolls = 10;
        long pollDelayMs = 5000;

        System.out.println("==============================================");
        System.out.println("  Quiz Leaderboard Builder");
        System.out.println("  regNo : " + regNo);
        System.out.println("  polls : " + totalPolls + " (5s apart)");
        System.out.println("==============================================\n");

        QuizPoller poller = new QuizPoller(regNo, baseUrl);
        ScoreAggregator aggregator = new ScoreAggregator();
        QuizSubmitter submitter = new QuizSubmitter(regNo, baseUrl);

        for (int i = 0; i < totalPolls; i++) {
            System.out.printf("%n[Poll %d/%d]%n", i, totalPolls - 1);

            try {
                PollResponse response = poller.poll(i);
                aggregator.process(response);
            } catch (RuntimeException e) {
                System.err.println("FATAL: " + e.getMessage());
                System.err.println("Aborting — not all polls completed.");
                System.exit(1);
            }

            if (i < totalPolls - 1) {
                System.out.println("  Waiting 5s before next poll...");
                try {
                    Thread.sleep(pollDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Interrupted during poll delay. Exiting.");
                    System.exit(1);
                }
            }
        }

        List<LeaderboardEntry> leaderboard = aggregator.buildLeaderboard();

        System.out.println("\n========== DEDUPLICATION SUMMARY ==========");
        System.out.println("Total events received : " + aggregator.getTotalEventsReceived());
        System.out.println("Duplicates skipped    : " + aggregator.getTotalEventsDuplicate());
        System.out.println("Unique events counted : " + aggregator.getTotalEventsAccepted());

        System.out.println("\n========== LEADERBOARD ==========");
        System.out.printf("%-5s %-20s %s%n", "Rank", "Participant", "Total Score");
        System.out.println("-".repeat(40));
        int rank = 1;
        for (LeaderboardEntry entry : leaderboard) {
            System.out.printf("%-5d %-20s %d%n", rank++, entry.getParticipant(), entry.getTotalScore());
        }
        System.out.println("-".repeat(40));
        System.out.println("Grand Total: " + aggregator.grandTotal());

        try {
            submitter.submit(leaderboard);
        } catch (Exception e) {
            System.err.println("Submit failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("\nDone.");
    }
}
