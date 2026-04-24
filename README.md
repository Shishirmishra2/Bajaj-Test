Quiz Leaderboard Builder

A Java-based solution that fetches quiz events from an API, calculates total scores, and submits them back.

Project Details
- Registration No.: Managed via `config.properties`
- Base URL: Managed via `config.properties`

Algorithmic Workflow:
1. Polling GET /quiz/messages 10 times (index 0 to 9), with a 5-second delay after each poll.
2. Deduplicating events based on the key `roundId | participant`.
3. Aggregating total scores per participant.
4. Sorting the final leaderboard in descending order (alphabetical ordering in case of a tie).
5. Making a POST /quiz/submit request.

Setup Secrets
To keep your details private, copy `config.properties.example` to `config.properties` and fill in your values:
```properties
reg.no=YOUR_REGISTRATION_NUMBER
api.base.url=https://devapigw.vidalhealthtpa.com/srm-quiz-task
```
`config.properties` is already added to `.gitignore` to prevent accidental commits.

Project Directory Structure
bajaj/
├── pom.xml                 # Maven configuration
├── lib/                    # Jackson JAR dependencies
├── config.properties.example # Template for secrets
├── config.properties       # Your private configuration (GIT IGNORED)
├── src/main/java/com/quiz/
│   ├── Main.java           # Orchestrator (Loads config)
│   ├── QuizPoller.java     # API GET handler
│   ├── ScoreAggregator.java # Deduplication and score logic
│   ├── QuizSubmitter.java   # API POST handler
│   ├── Event.java          # Event DTO
│   ├── PollResponse.java   # API GET response DTO
│   └── LeaderboardEntry.java # Leaderboard Entry DTO
└── README.md               # This document

How To Execute

Recommended Method: Maven
With Maven installed:
```powershell
mvn package -q
java -jar target/quiz-app-1.0-SNAPSHOT.jar
```

Option 2: Direct Java Execution (No Maven)
Using pre-downloaded JARs in the `lib/` folder:
```powershell
# Compile
javac -cp "lib\*" -d out src\main\java\com\quiz\*.java

# Run
java -cp "out;lib\*" com.quiz.Main
```

Troubleshooting
- HTTP 503: Indicates the API server is unavailable or the quiz window is closed.
- config.properties: Ensure this file exists in the project root before running.
