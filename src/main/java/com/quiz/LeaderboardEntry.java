package com.quiz;

public class LeaderboardEntry implements Comparable<LeaderboardEntry> {

    private String participant;
    private int totalScore;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String participant, int totalScore) {
        this.participant = participant;
        this.totalScore = totalScore;
    }

    public String getParticipant() { return participant; }
    public int    getTotalScore()  { return totalScore; }

    public void setParticipant(String participant) { this.participant = participant; }
    public void setTotalScore(int totalScore)      { this.totalScore = totalScore; }

    @Override
    public int compareTo(LeaderboardEntry other) {
        int cmp = Integer.compare(other.totalScore, this.totalScore);
        if (cmp != 0) return cmp;
        return this.participant.compareTo(other.participant);
    }

    @Override
    public String toString() {
        return String.format("%-20s %d", participant, totalScore);
    }
}
