import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Player implements Runnable{
    
    private final int playerNumber;
    private final CardDeck left;
    private final CardDeck right;
    private final AtomicInteger winner; //shared by all players, 0 means there is yet a winner
    private final CountDownLatch startLatch;
    private final List<Card> hand = new ArrayList<>();

    // for logging method
    private PrintWriter out;

    public Player(
        int playerNumber, CardDeck left, CardDeck right, AtomicInteger winner, 
    CountDownLatch startLatch) {
        this.playerNumber = playerNumber;
        this.left = left;
        this.right = right;
        this.winner = winner;
        this.startLatch = startLatch;
    }

    private void log(String line) {
        out.println(line);
        // to define out in run function
    }

    public void addCard(Card card) {
        hand.add(card);
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

    private Card cardToDiscard() {
        for (Card c : hand) {
            if(c.getValue() != playerNumber){
                return c;
            }
        }
        throw new IllegalStateException("Player " + playerNumber + " has no card it is allowed to discard");
    }

    private Card discardCard() {
        Card discard = cardToDiscard();
        hand.remove(discard);
        right.addToBottom(discard);
        return discard;
    }

    private Card cardToDraw() {
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
        CardDeck first = left;
        CardDeck second = right;
        synchronized (first) {
            synchronized (second) {
                Card drawn = cardToDraw();
                if (drawn == null) {
                    return;
                }
                Card discarded = discardCard();

                // To be changed to proper logging methods
                log("player " + playerNumber + " draws a " + drawn + " from deck " + left.getId());
                log("player " + playerNumber + " discards a " + discarded + " to deck " + right.getId());
                log("player " + playerNumber + " current hand is " + handToString());
                log("deck " + playerNumber + " current deck is " + left.contentsString());
            }
        }
    }

    @Override
    public void run(){

        try(PrintWriter writer = new PrintWriter(new FileWriter("player" + playerNumber + "_output.txt"))) {
            out = writer;
                
            // 1, initial hand
            log("player " + playerNumber + " initial hand " + handToString());

            // 2, check for immediate win
            if (hasFourOfAKind()) {
                winner.compareAndSet(0, playerNumber);
            }

            // 3, wait until all players have checked their dealt hand
            startLatch.countDown();
            startLatch.await();

            // 4, play until someone has won
            while (winner.get() == 0) {
                takeTurn();
                if (hasFourOfAKind()) {
                    winner.compareAndSet(0, playerNumber);
                }
            }

            // 5, finishing lines when game is won 
            int w = winner.get();
            if (w == playerNumber) {
                System.out.println("player " + playerNumber + " wins");
                log("player " + playerNumber + " wins");
                log("player " + playerNumber + " exits");
                log("player " + playerNumber + " final hand: " + handToString());
            } else {
                log("player " + w + " has informed player " + playerNumber + " that player " + w + " has won");
                log("player " + playerNumber + " exits");
                log("player " + playerNumber + " hand: " + handToString());
            }

        } catch (IOException e) {
            System.err.println("player " + playerNumber + " could not write to output file: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}