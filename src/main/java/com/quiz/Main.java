package com.quiz;

import java.util.List;

public class Main {

    private static final String REG_NO = "RA2311047010171";
    private static final int TOTAL_POLLS = 10;
    private static final long POLL_DELAY_MS = 5000;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Quiz Leaderboard Builder");
        System.out.println("  regNo : " + REG_NO);
        System.out.println("  polls : " + TOTAL_POLLS + " (5s apart)");
        System.out.println("==============================================\n");

        QuizPoller poller = new QuizPoller(REG_NO);
        ScoreAggregator aggregator = new ScoreAggregator();
        QuizSubmitter submitter = new QuizSubmitter(REG_NO);

        for (int i = 0; i < TOTAL_POLLS; i++) {
            System.out.printf("%n[Poll %d/%d]%n", i, TOTAL_POLLS - 1);

            try {
                PollResponse response = poller.poll(i);
                aggregator.process(response);
            } catch (RuntimeException e) {
                System.err.println("FATAL: " + e.getMessage());
                System.err.println("Aborting — not all polls completed.");
                System.exit(1);
            }

            if (i < TOTAL_POLLS - 1) {
                System.out.println("  Waiting 5s before next poll...");
                try {
                    Thread.sleep(POLL_DELAY_MS);
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
