import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class CardGame {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // implement readPlayerCount method
        int n = readPlayerCount(scanner);
        // implement readPack method
        List<card> pack = readPack(scanner, n);

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

    static List<card> readPack(Scanner scanner, int n) {
        while (true) {
            System.out.println("Please enter location of pack to load:");
            String path = scanner.nextLine().trim();
            try{ 
                // to implement loadPack method
                return loadPack(path, n);
            } catch (IOException e) {
                System.out.println("Could not read file :" + path);
            } catch (IllegalArgumentException e){
                System.out.println("Invalid pack: " + e.getMessage());
            }
        }
    }



}