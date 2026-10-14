abstract class Player extends Thread{
    
    public final int playerNumber = n;
    public final String playerName = "player" + playerNumber;

    @Override
    public void run() {
        // implement the actual running of the thread here
        // current is placeholder
        System.out.println("Hi");
    }

    public String getPlayerName() {
        return playerName;
    }


}