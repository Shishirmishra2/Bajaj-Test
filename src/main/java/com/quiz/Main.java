package com.quiz;

import java.util.List;

public class Main {
    private static final String REG_NO = "RA2311047010171";

    public static void main(String[] args) {
        System.out.println("Starting Quiz Leaderboard Builder for: " + REG_NO);

        QuizPoller poller = new QuizPoller(REG_NO);
        ScoreAggregator aggregator = new ScoreAggregator();
        QuizSubmitter submitter = new QuizSubmitter(REG_NO);

        for (int i = 0; i < 10; i++) {
            System.out.println("\nPoll " + i + " of 9...");
            try {
                PollResponse response = poller.poll(i);
                aggregator.process(response);
            } catch (Exception e) {
                System.err.println("Error during poll: " + e.getMessage());
                return;
            }

            if (i < 9) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        List<LeaderboardEntry> leaderboard = aggregator.buildLeaderboard();

        System.out.println("\n--- Summary ---");
        System.out.println("Total: " + aggregator.getTotalEventsReceived());
        System.out.println("Duplicates: " + aggregator.getTotalEventsDuplicate());
        System.out.println("Accepted: " + aggregator.getTotalEventsAccepted());

        System.out.println("\n--- Leaderboard ---");
        for (int i = 0; i < leaderboard.size(); i++) {
            LeaderboardEntry e = leaderboard.get(i);
            System.out.println((i + 1) + ". " + e.getParticipant() + ": " + e.getTotalScore());
        }

        try {
            submitter.submit(leaderboard);
        } catch (Exception e) {
            System.err.println("Failed to submit: " + e.getMessage());
        }
    }
}
