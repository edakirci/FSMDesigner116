public interface Transition {
    char getSymbol();
    State getCurrentState();
    State getNextState();
}
