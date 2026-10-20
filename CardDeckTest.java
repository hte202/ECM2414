import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class CardDeckTest {

    // FIFO behaviour

    @Test
    void drawsCardsInTheOrderTheyWereAdded() {
        CardDeck deck = new CardDeck(1);
        deck.addToBottom(new Card(3));
        deck.addToBottom(new Card(7));
        deck.addToBottom(new Card(2));

        assertEquals(3, deck.drawFromTop().getValue());
        assertEquals(7, deck.drawFromTop().getValue());
        assertEquals(2, deck.drawFromTop().getValue());
    }

    @Test
    void drawingFromAnEmptyDeckReturnsNull() {
        CardDeck deck = new CardDeck(1);
        assertNull(deck.drawFromTop());
    }

    @Test
    void deckIsEmptyAgainAfterAllCardsAreDrawn() {
        CardDeck deck = new CardDeck(1);
        deck.addToBottom(new Card(4));
        deck.drawFromTop();
        assertNull(deck.drawFromTop());
    }

    // output formatting

    @Test
    void contentsStringListsCardsFromTopToBottom() {
        CardDeck deck = new CardDeck(2);
        deck.addToBottom(new Card(1));
        deck.addToBottom(new Card(3));
        deck.addToBottom(new Card(3));
        deck.addToBottom(new Card(7));

        assertEquals("1 3 3 7", deck.contentsString());
    }

    @Test
    void contentsStringOfAnEmptyDeckIsEmpty() {
        assertEquals("", new CardDeck(1).contentsString());
    }

    @Test
    void contentsStringDoesNotRemoveCards() {
        CardDeck deck = new CardDeck(1);
        deck.addToBottom(new Card(5));
        deck.contentsString();
        assertEquals(5, deck.drawFromTop().getValue());
    }

    // thread safety

    @Test
    void concurrentDiscardsLoseNoCards() throws InterruptedException {
        CardDeck deck = new CardDeck(1);
        int threadCount = 4;
        int cardsPerThread = 1000;

        List<Thread> workers = new ArrayList<>();
        for (int t = 0; t < threadCount; t++) {
            Thread w = new Thread(() -> {
                for (int i = 0; i < cardsPerThread; i++) {
                    deck.addToBottom(new Card(i));
                }
            });
            workers.add(w);
            w.start();
        }
        for (Thread w : workers) {
            w.join();
        }

        int count = 0;
        while (deck.drawFromTop() != null) {
            count++;
        }
        assertEquals(threadCount * cardsPerThread, count);
    }

    @Test
    void concurrentDrawsNeverHandOutTheSameCardTwice() throws InterruptedException {
        CardDeck deck = new CardDeck(1);
        int total = 4000;
        for (int i = 0; i < total; i++) {
            deck.addToBottom(new Card(i));
        }

        AtomicInteger drawn = new AtomicInteger(0);
        List<Thread> workers = new ArrayList<>();
        for (int t = 0; t < 4; t++) {
            Thread w = new Thread(() -> {
                while (deck.drawFromTop() != null) {
                    drawn.incrementAndGet();
                }
            });
            workers.add(w);
            w.start();
        }
        for (Thread w : workers) {
            w.join();
        }

        assertEquals(total, drawn.get());
    }
}