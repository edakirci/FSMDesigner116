public interface Transition extends java.io.Serializable {
    char getSymbol();
    State getCurrentState();
    State getNextState();
}
