import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CardGameTest {

    @TempDir
    Path tempDir;

    //  helpers 

    private Path writePack(String... lines) throws IOException {
        Path p = tempDir.resolve("pack.txt");
        Files.write(p, Arrays.asList(lines));
        return p;
    }

    private static List<Card> cards(int... values) {
        List<Card> pack = new ArrayList<>();
        for (int v : values) {
            pack.add(new Card(v));
        }
        return pack;
    }

    private static List<String> readOutput(String fileName) throws IOException {
        return Files.readAllLines(Paths.get(fileName));
    }

    // values after the ':' on a line, e.g. "player 1 hand: 2 2 3 5" -> [2, 2, 3, 5]
    private static List<Integer> valuesAfterColon(String line) {
        List<Integer> result = new ArrayList<>();
        String rest = line.substring(line.indexOf(':') + 1).trim();
        if (rest.isEmpty()) {
            return result;
        }
        for (String s : rest.split(" ")) {
            result.add(Integer.parseInt(s));
        }
        return result;
    }

    @AfterEach
    void deleteOutputFiles() throws IOException {
        for (int i = 1; i <= 4; i++) {
            Files.deleteIfExists(Paths.get("player" + i + "_output.txt"));
            Files.deleteIfExists(Paths.get("deck" + i + "_output.txt"));
        }
    }

    // pack loading: valid input 

    @Test
    void validPackIsLoadedInOrderIncludingZero() throws IOException {
        Path pack = writePack("0", "1", "2", "3", "4", "5", "6", "7");
        List<Card> loaded = CardGame.loadPack(pack.toString(), 1);

        assertEquals(8, loaded.size());
        assertEquals(0, loaded.get(0).getValue());
        assertEquals(7, loaded.get(7).getValue());
    }

    @Test
    void trailingBlankLineIsAccepted() throws IOException {
        Path pack = writePack("1", "1", "1", "1", "2", "2", "2", "2", "");
        assertEquals(8, CardGame.loadPack(pack.toString(), 1).size());
    }

    @Test
    void valuesLargerThanNAreAccepted() throws IOException {
        Path pack = writePack("100", "1", "1", "1", "2", "2", "2", "2");
        assertEquals(100, CardGame.loadPack(pack.toString(), 1).get(0).getValue());
    }

    // pack loading: invalid input 

    @Test
    void packWithTooFewLinesIsRejected() throws IOException {
        Path pack = writePack("1", "1", "1", "1", "2", "2", "2");
        assertThrows(IllegalArgumentException.class, () -> CardGame.loadPack(pack.toString(), 1));
    }

    @Test
    void packWithTooManyLinesIsRejected() throws IOException {
        Path pack = writePack("1", "1", "1", "1", "2", "2", "2", "2", "3");
        assertThrows(IllegalArgumentException.class, () -> CardGame.loadPack(pack.toString(), 1));
    }

    @Test
    void negativeValueIsRejected() throws IOException {
        Path pack = writePack("1", "1", "1", "-1", "2", "2", "2", "2");
        assertThrows(IllegalArgumentException.class, () -> CardGame.loadPack(pack.toString(), 1));
    }

    @Test
    void nonIntegerValueIsRejected() throws IOException {
        Path pack = writePack("1", "1", "abc", "1", "2", "2", "2", "2");
        assertThrows(IllegalArgumentException.class, () -> CardGame.loadPack(pack.toString(), 1));
    }

    @Test
    void missingFileThrowsIOException() {
        String missing = tempDir.resolve("does_not_exist.txt").toString();
        assertThrows(IOException.class, () -> CardGame.loadPack(missing, 1));
    }

    // full games

    @Test
    void immediateWinIsDetectedAndAllFilesAreWritten() throws IOException {
        // player 1 is dealt 1 1 1 1, player 2 is dealt 2 3 2 3
        List<Card> pack = cards(1, 2, 1, 3, 1, 2, 1, 3, 5, 5, 6, 6, 7, 7, 8, 8);

        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> CardGame.playGame(2, pack));

        assertEquals(List.of(
                "player 1 initial hand 1 1 1 1",
                "player 1 wins",
                "player 1 exits",
                "player 1 final hand: 1 1 1 1"), readOutput("player1_output.txt"));

        assertEquals(List.of(
                "player 2 initial hand 2 3 2 3",
                "player 1 has informed player 2 that player 1 has won",
                "player 2 exits",
                "player 2 hand: 2 3 2 3"), readOutput("player2_output.txt"));

        assertEquals(List.of("deck1 contents: 5 6 7 8"), readOutput("deck1_output.txt"));
        assertEquals(List.of("deck2 contents: 5 6 7 8"), readOutput("deck2_output.txt"));
    }

    @RepeatedTest(20)
    void fullGameHasOneWinnerFourCardHandsAndConservesCards() throws IOException {
        // no immediate win: player 1 dealt 1 2 1 2, player 2 dealt 2 1 2 1
        List<Card> pack = cards(1, 2, 2, 1, 1, 2, 2, 1, 1, 2, 1, 2, 1, 2, 1, 2);
        int n = 2;

        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> CardGame.playGame(n, pack));

        int winners = 0;
        List<Integer> allCards = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            List<String> lines = readOutput("player" + i + "_output.txt");

            assertTrue(lines.get(0).startsWith("player " + i + " initial hand "));
            assertEquals("player " + i + " exits", lines.get(lines.size() - 2));

            if (lines.contains("player " + i + " wins")) {
                winners++;
                List<Integer> finalHand = valuesAfterColon(lines.get(lines.size() - 1));
                assertTrue(lines.get(lines.size() - 1).startsWith("player " + i + " final hand: "));
                assertEquals(1, finalHand.stream().distinct().count(), "winner must hold four of a kind");
            }

            List<Integer> hand = valuesAfterColon(lines.get(lines.size() - 1));
            assertEquals(4, hand.size(), "player " + i + " must finish with four cards");
            allCards.addAll(hand);
        }

        for (int d = 1; d <= n; d++) {
            List<String> lines = readOutput("deck" + d + "_output.txt");
            assertEquals(1, lines.size());
            assertTrue(lines.get(0).startsWith("deck" + d + " contents:"));
            allCards.addAll(valuesAfterColon(lines.get(0)));
        }

        assertEquals(1, winners, "exactly one player should declare a win");
        assertEquals(8 * n, allCards.size(), "all 8n cards must be accounted for");
        assertEquals(8, allCards.stream().filter(v -> v == 1).count());
        assertEquals(8, allCards.stream().filter(v -> v == 2).count());
    }
}