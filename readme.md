# ECM2414 cardsTest.zip

## Reqirements

- JDK [25.0.1]
- JUnit 5 (Jupiter), run through the JUnit Platform Console Launcher
  version 1.10.2. The launcher jar is included in this zip, so no
  download is needed. If it is missing, it can be downloaded from:
  https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar

No other libraries or build tools (Maven, Gradle) are required.

## Dependency

The JUnit tests are dependant on the jar file:
```
junit-platform-console-standalone-1.10.2.jar
```

## Running the tests

### How to compile

In the terminal in the folder containing the files run:
```
javac -cp junit-platform-console-standalone-1.10.2.jar *.java
```

This will compile all of the classes.

### How to run

In the terminal of the folder contraining the compiled files run:
```
java -jar junit-platform-console-standalone-1.10.2.jar -cp . --scan-classpath
```
this runs all available JUnit tests, or:
```
java -jar junit-platform-console-standalone-1.10.2.jar execute -cp . --select-class TestFileName
```
runs an individual test file. 

## Contents of file

```
Production code (source and bytecode):
  Card.java / Card.class
  CardDeck.java / CardDeck.class
  Player.java / Player.class
  CardGame.java / CardGame.class
 
Test code (source and bytecode):
  CardDeckTest.java / CardDeckTest.class
  PlayerTest.java / PlayerTest.class
  CardGameTest.java / CardGameTest.class
```

## Files created during testing

- The full-game tests in CardGameTest run real games, which write
  player1_output.txt, player2_output.txt, deck1_output.txt and
  deck2_output.txt to the current folder. These are checked by the
  tests and then deleted automatically after each test.
- Pack files used by the tests are created in a temporary folder by
  JUnit (@TempDir) and removed afterwards, so no pack files need to be
  supplied.


## Running the game itself

From the folder containing cards.jar:

  java -jar cards.jar

The program asks for the number of players, then the location of a
pack file. A valid pack is a plain text file with exactly 8n lines,
each containing one non-negative integer. Invalid input is rejected
and the user is asked again.

At the end of the game, one output file per player
(playerX_output.txt) and one per deck (deckX_output.txt) are written
to the current folder.