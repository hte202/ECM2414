import java.io.*;
import java.util.*;

public abstract class CardDeck{

    public int numberOfCards;
}

    public static void main(String[] args){
        File myObj = new File("PlaceholderFileName.txt");
        try(Scanner scanner = new Scanner(myObj)){
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
            }
        } catch(FileNotFoundException){
            e.printStackTrace();
            }
    }
}

