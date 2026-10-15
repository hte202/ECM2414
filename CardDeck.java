import java.util.*;

public class CardDeck{

    private final int id;
    private final Deque<Card> cards = new ArrayDeque<>();

    // Class contructor
    public CardDeck(int id) {
        this.id = id;
    }

    // Getter for id
    public int getId() {
        return id;
    }


    // Synchronized methods
    public synchronized void addToBottom(Card c) {
        cards.addLast(c);
    }
    public synchronized Card drawFromTop() {
        return cards.pollFirst();
    }
    public synchronized String contentsString() {
        // to be completed
    } 
}

