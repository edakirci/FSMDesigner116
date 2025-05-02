import java.util.*;
public class ConcreteTransition implements Transition{
    private final char symbol;
    private final State currentState;
    private final State nextState;
    public ConcreteTransition(char symbol, State currentState, State nextState) {
        this.symbol = Character.toUpperCase(symbol);
        this.currentState = currentState;
        this.nextState = nextState;
    }
    public char getSymbol() { return symbol; }
    public State getCurrentState() { return currentState; }
    public State getNextState() { return nextState; }
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transition)) return false;
        Transition t = (Transition) o;
        return symbol == t.getSymbol() &&
                currentState.equals(t.getCurrentState());
    }
    public int hashCode() {
        return Objects.hash(Character.toUpperCase(symbol), currentState.getName().toUpperCase());
    }
}
