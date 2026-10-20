import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class CardGame {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = readPlayerCount(scanner);
        List<Card> pack = readPack(scanner, n);

        try{
            playGame(n, pack);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Game was interrupted.");
        }  
    }

    static int readPlayerCount(Scanner scanner) {
        while (true) {
            System.out.println("Please enter the number of players:");
            String line = scanner.nextLine().trim();
            try {
                int n = Integer.parseInt(line);
                if (n > 0) {
                    return n;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number of players, please enter a positive integer.");
            }    
        }
    }

    static List<Card> readPack(Scanner scanner, int n) {
        while (true) {
            System.out.println("Please enter location of pack to load:");
            String path = scanner.nextLine().trim();
            try{ 
                return loadPack(path, n);
            } catch (IOException e) {
                System.out.println("Could not read file :" + path);
            } catch (IllegalArgumentException e){
                System.out.println("Invalid pack: " + e.getMessage());
            }
        }
    }

    public static List<Card> loadPack(String path, int n) throws IOException{
        List<String> lines = new ArrayList<>(Files.readAllLines(Paths.get(path)));

        while (!lines.isEmpty() && lines.get(lines.size() - 1).trim().isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        if (lines.size() != 8 * n) {
            throw new IllegalArgumentException("pack must have " + (8 * n) + "lines, found " + lines.size());
        } 

        List<Card> pack = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            int value;
            try {
                value = Integer.parseInt(lines.get(i).trim());
            } 
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("line " + (i + 1) + " is not an integer");
            }
            if (value < 0) {
                throw new IllegalArgumentException("line " + (i + 1) + " is negative");
            }
            pack.add(new Card(value));
        }
        return pack;
    }

    static void playGame(int n, List<Card> pack) throws InterruptedException {

        List<CardDeck> decks = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            decks.add(new CardDeck(i));
        }

        // objects shared by all threads
        AtomicInteger winner = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(n);

        // players 1 to n: player i draws from deck i, discards to deck i+1 (player n wraps to deck 1)
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            CardDeck left = decks.get(i - 1);
            CardDeck right = decks.get(i % n);
            players.add(new Player(i, left, right, winner, startLatch));
        }

        // deal hands round-robin
        // first 4n cards
        for (int i = 0; i < 4 * n; i++) {
            players.get(i % n).addCard(pack.get(i));
        }
        // fill decks round-robin
        // last 4n cards
        for (int i = 4 * n; i < 8 * n; i++) {
            decks.get((i - 4 * n) % n).addToBottom(pack.get(i)); 
        }

        // start every player thread
        List<Thread> threads = new ArrayList<>();
        for (Player p : players) {
            Thread t = new Thread(p);
            threads.add(t);
            t.start();
        }

        // wait for all players to finish before touching the decks
        for (Thread t : threads) {
            t.join();
        }

        writeDeckFiles(decks);
    }

    static void writeDeckFiles(List<CardDeck> decks) {
        for (CardDeck deck : decks) {
            try (PrintWriter out = new PrintWriter(new FileWriter("deck" + deck.getId() + "_output.txt"))) {
                out.println("deck" + deck.getId() + " contents: " + deck.contentsString());
            } catch (IOException e) {
                System.err.println("Could not write output for deck " + deck.getId() + ": " + e.getMessage());
            }
        }
    }
}