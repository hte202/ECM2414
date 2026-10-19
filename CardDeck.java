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

    // Method to add card to bottom of deck (back of queue)
    public synchronized void addToBottom(Card c) {
        cards.addLast(c);
    }
    // Method to draw a card from the top of the deck (front of queue)
    public synchronized Card drawFromTop() {
        return cards.pollFirst();
    }

    // Method to write deck to a string
    public synchronized String contentsString() {
        StringBuilder sb = new StringBuilder();
        for (Card c : cards) {
            if (sb.length() > 0 ) {
                sb.append(" ");
            }
            sb.append(c.getValue());
        }
        return sb.toString();
    } 
}

