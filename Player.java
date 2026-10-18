import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Player implements Runnable{
    
    private final int playerNumber;
    private final CardDeck left;
    private final CardDeck right;
    private final AtomicInteger winner; //shared by all players, 0 means there is yet a winner
    private final CountDownLatch instantWinner;
    private final List<Card> hand = new ArrayList<>();

    public Player(
        int playerNumber, CardDeck left, CardDeck right, AtomicInteger winner, 
    CountDownLatch instantWinner) {
        this.playerNumber = playerNumber;
        this.left = left;
        this.right = right;
        this.winner = winner;
        this.instantWinner = instantWinner;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public boolean hasFourOfAKind() {
        int first = hand.get(0).getValue();
        for (Card c : hand ){
            if (c.getValue() != first) {
                return false;
            }
        }
        return true;
    }

    Card cardToDiscard() {
        for (Card c : hand) {
            if(c.getValue() != playerNumber){
                return c;
            }
        }
        throw new IllegalStateException("Player " + playerNumber + " has no card it is allowed to discard");
    }

    Card cardToDraw() {
        Card drawn = left.drawFromTop();
        if (drawn != null) {
            hand.add(drawn);
        }
        return drawn;          
    }

    public String handToString() {
        StringBuilder sb = new StringBuilder();
        for (Card c : hand) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(c.getValue());
        }
        return sb.toString();
    }

    void takeTurn() {
        CardDeck first = left.getId() <= right.getId() ? left : right;
        CardDeck second = (first == left) ? right : left;

        synchronized (first) {
            synchronized (second) {
                Card drawn = cardToDraw();
                if (drawn == null) {
                    return;
                }
                Card discarded = cardToDiscard();

                // To be changed to proper logging methods
                System.out.println("player " + playerNumber + " draws a " + drawn + " from deck " + left.getId());
                System.out.println("player " + playerNumber + " discards a " + discarded + " to deck " + right.getId());
                System.out.println("player " + playerNumber + "'s current hand is " + handToString());
            }
        }
    }

    @Override
    public void run(){
        // to be completed
    }
}