package com.quiz;

import java.util.*;

public class ScoreAggregator {

    private final Set<String> seenKeys = new HashSet<>();
    private final Map<String, Integer> scores = new LinkedHashMap<>();
    private int totalEventsReceived = 0;
    private int totalEventsDuplicate = 0;

    public void process(PollResponse response) {
        if (response.getEvents() == null) return;

        for (Event event : response.getEvents()) {
            totalEventsReceived++;
            String key = event.dedupKey();

            if (seenKeys.contains(key)) {
                System.out.printf("    [DEDUP] Skipping duplicate: %s%n", key);
                totalEventsDuplicate++;
                continue;
            }

            seenKeys.add(key);
            scores.merge(event.getParticipant(), event.getScore(), Integer::sum);
            System.out.printf("    [NEW]   Accepted: %-30s score=+%d%n", key, event.getScore());
        }
    }

    public List<LeaderboardEntry> buildLeaderboard() {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            leaderboard.add(new LeaderboardEntry(entry.getKey(), entry.getValue()));
        }
        Collections.sort(leaderboard);
        return leaderboard;
    }

    public int grandTotal() {
        return scores.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getTotalEventsReceived()  { return totalEventsReceived; }
    public int getTotalEventsDuplicate() { return totalEventsDuplicate; }
    public int getTotalEventsAccepted()  { return totalEventsReceived - totalEventsDuplicate; }
}
