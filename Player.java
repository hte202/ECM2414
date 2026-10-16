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

    @Override
    public void run(){
        // to be completed
    }
}