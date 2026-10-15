public final class Card{
    
    private final int value;
    
    // Class constructor
    public Card(int value){
        this.value = value;
    }

    // Getter for value
    public int getValue(){
        return value;
    }

    @Override
    public String toString(){
        return String.valueOf(value);
    }
}