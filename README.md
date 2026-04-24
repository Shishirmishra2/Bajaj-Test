Quiz Leaderboard Builder

A Java-based solution that fetches quiz events from an API, duplicates them, calculates the total scores, and then sends them back to the API.

Project Details
Registration No.: RA2311047010171
Base URL for API Calls : `https://devapigw.vidalhealthtpa.com/srm-quiz-task`
Algorithmic Workflow :
  1. Polling GET /quiz/messages for 10 times (index 0 to 9), with a 5-second delay after each poll.
  2. Deduplicating events based on the key roundId | participant.
  3. Aggregating the total score of all events for a participant.
  4. Sorting the final leaderboard in descending order according to the total score (alphabetical ordering in case of a tie).
  5. Finally, making a POST /quiz/submit request.

Project Directory Structure

bajaj/
pom.xml                                  # Maven configuration
lib/                                     # Jackson JAR dependencies (for non-Maven builds)
src/main/java/com/quiz/
Main.java                                # Orchestrator
QuizPoller.java                          # API GET handler with retries
ScoreAggregator.java                     # API event deduplication and total score calculation
QuizSubmitter.java                       # API POST handler
Event.java                               # Event DTO
PollResponse.java                        # API GET response DTO
LeaderboardEntry.java                    # Leaderboard Entry DTO
README.md                                # This document


How To Execute

Recommended Method: Maven
With Maven installed:
powershell

java -jar target/quiz-app-1.0-SNAPSHOT.jar


Option 2: Direct Java Execution (No Maven)

If Maven is not available, use the pre-downloaded JARs in the `lib/` folder:

powershell

Compile the source code
javac -cp "lib\*" -d out src\main\java\com\quiz\*.java

Run the application
java -cp "out;lib\*" com.quiz.Main



Troubleshooting
HTTP 503 (no available server): This indicates the quiz API server is currently unavailable or the quiz window is not active. The application handles this by retrying 3 times before exiting.
Dependencies: The project uses Jackson for JSON parsing. Ensure jackson-databind, jackson-core, and jackson-annotations are in the classpath.
