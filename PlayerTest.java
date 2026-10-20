import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class PlayerTest {

    // helper methods

    private Player newPlayer(int number, CardDeck left, CardDeck right, int... cards) {
        Player p = new Player(number, left, right, new AtomicInteger(0), new CountDownLatch(1));
        for (int v : cards) {
            p.addCard(new Card(v));
        }
        return p;
    }

    private CardDeck deckWith(int id, int... cards) {
        CardDeck deck = new CardDeck(id);
        for (int v : cards) {
            deck.addToBottom(new Card(v));
        }
        return deck;
    }

    private static List<Integer> values(String spaceSeparated) {
        List<Integer> result = new ArrayList<>();
        if (spaceSeparated.trim().isEmpty()) {
            return result;
        }
        for (String s : spaceSeparated.trim().split(" ")) {
            result.add(Integer.parseInt(s));
        }
        return result;
    }

    // win detection

    @Test
    void fourPreferredCardsIsAWin() {
        Player p = newPlayer(1, new CardDeck(1), new CardDeck(2), 1, 1, 1, 1);
        assertTrue(p.hasFourOfAKind());
    }

    @Test
    void fourMatchingNonPreferredCardsIsStillAWin() {
        Player p = newPlayer(1, new CardDeck(1), new CardDeck(2), 7, 7, 7, 7);
        assertTrue(p.hasFourOfAKind());
    }

    @Test
    void threeMatchingCardsIsNotAWin() {
        Player p = newPlayer(2, new CardDeck(2), new CardDeck(3), 2, 2, 2, 5);
        assertFalse(p.hasFourOfAKind());
    }

    @Test
    void nonPreferredCardsAreNotHeldIndefinitely() {
        // player 1 holds two non-preferred cards and keeps drawing 1s
        // both non-preferred cards must leave, oldest first
        CardDeck left = deckWith(1, 1, 1);
        CardDeck right = new CardDeck(2);
        Player p = newPlayer(1, left, right, 5, 1, 1, 6);

        p.takeTurn();
        p.takeTurn();

        assertEquals("5 6", right.contentsString());
        assertEquals("1 1 1 1", p.handToString());
        assertTrue(p.hasFourOfAKind());
    }

    // the atomic turn

    @Test
    void aTurnDrawsFromLeftDiscardsToRightAndKeepsFourCards() {
        CardDeck left = deckWith(1, 5, 8);
        CardDeck right = new CardDeck(2);
        Player p = newPlayer(1, left, right, 1, 3, 1, 4);

        p.takeTurn();

        assertEquals(4, values(p.handToString()).size());
        assertEquals("8", left.contentsString());       // 5 was drawn from the top
        assertEquals("3", right.contentsString());      // oldest non-preferred discarded
        assertEquals("1 1 4 5", p.handToString());
    }

    @Test
    void aTurnWithAnEmptyLeftDeckChangesNothing() {
        CardDeck left = new CardDeck(1);
        CardDeck right = new CardDeck(2);
        Player p = newPlayer(1, left, right, 1, 3, 1, 4);

        p.takeTurn();

        assertEquals("1 3 1 4", p.handToString());
        assertEquals("", right.contentsString());
    }

    @Test
    void singlePlayerRingConservesCardsAndDoesNotDeadlock() {
        // n = 1: the player's left and right deck are the same object
        CardDeck deck = deckWith(1, 7, 8, 9, 2);
        Player p = newPlayer(1, deck, deck, 3, 4, 5, 6);

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            for (int i = 0; i < 100; i++) {
                p.takeTurn();
            }
        });

        List<Integer> all = new ArrayList<>(values(p.handToString()));
        all.addAll(values(deck.contentsString()));
        all.sort(null);

        assertEquals(4, values(p.handToString()).size());
        assertEquals(List.of(2, 3, 4, 5, 6, 7, 8, 9), all);
    }

    @Test
    void twoPlayersTakingTurnsConcurrentlyDoNotDeadlockAndConserveCards() {
        // ring of two: p1 draws deck1 so discards to deck2, p2 draws deck2 so discards to deck1.
        // If both locked "left then right" this would deadlock; ordered locking prevents it.
        // No card is a 1 or 2, so neither player can complete a win mid-test.
        CardDeck d1 = deckWith(1, 11, 12, 13, 14);
        CardDeck d2 = deckWith(2, 15, 16, 17, 18);
        Player p1 = newPlayer(1, d1, d2, 3, 4, 5, 6);
        Player p2 = newPlayer(2, d2, d1, 7, 8, 9, 10);

        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            Thread t1 = new Thread(() -> { for (int i = 0; i < 10000; i++) p1.takeTurn(); });
            Thread t2 = new Thread(() -> { for (int i = 0; i < 10000; i++) p2.takeTurn(); });
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        });

        assertEquals(4, values(p1.handToString()).size());
        assertEquals(4, values(p2.handToString()).size());

        List<Integer> all = new ArrayList<>();
        all.addAll(values(p1.handToString()));
        all.addAll(values(p2.handToString()));
        all.addAll(values(d1.contentsString()));
        all.addAll(values(d2.contentsString()));
        all.sort(null);

        List<Integer> expected = new ArrayList<>();
        for (int v = 3; v <= 18; v++) {
            expected.add(v);
        }
        assertEquals(expected, all);
    }
}