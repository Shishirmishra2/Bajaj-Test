# Quiz Leaderboard Builder

A Java application that polls a quiz API, aggregates scores with deduplication, and submits a final leaderboard.

## Project Overview
- **Registration Number:** RA2311047010171
- **API Base URL:** `https://devapigw.vidalhealthtpa.com/srm-quiz-task`
- **Logic:**
  1. Polls `GET /quiz/messages` 10 times (index 0-9) with a 5-second delay between each.
  2. Deduplicates events using a composite key: `roundId | participant`.
  3. Aggregates scores per participant.
  4. Sorts the leaderboard descending by total score (with alphabetical tie-breaking).
  5. Submits the final leaderboard via `POST /quiz/submit`.

## Project Structure
```
bajaj/
├── pom.xml                 # Maven configuration
├── lib/                    # Jackson JAR dependencies (for non-Maven environments)
├── src/main/java/com/quiz/
│   ├── Main.java           # Orchestrator
│   ├── QuizPoller.java     # Handles API GET requests with retries
│   ├── ScoreAggregator.java # Deduplication and score summation logic
│   ├── QuizSubmitter.java   # Handles final API POST request
│   ├── Event.java          # Event Data Transfer Object
│   ├── PollResponse.java   # API Response Data Transfer Object
│   └── LeaderboardEntry.java # Leaderboard Entry Data Transfer Object
└── README.md               # This file
```

## How to Run

### Option 1: Using Maven (Recommended)
If you have Maven installed:
```powershell
# Build the project
mvn package -q

# Run the application
java -jar target/quiz-app-1.0-SNAPSHOT.jar
```

### Option 2: Direct Java Execution (No Maven)
If Maven is not available, use the pre-downloaded JARs in the `lib/` folder:
```powershell
# Compile the source code
javac -cp "lib\*" -d out src\main\java\com\quiz\*.java

# Run the application
java -cp "out;lib\*" com.quiz.Main
```

## Troubleshooting
- **HTTP 503 (no available server):** This indicates the quiz API server is currently unavailable or the quiz window is not active. The application handles this by retrying 3 times before exiting.
- **Dependencies:** The project uses Jackson for JSON parsing. Ensure `jackson-databind`, `jackson-core`, and `jackson-annotations` are in the classpath.
