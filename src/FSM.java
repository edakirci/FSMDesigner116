import java.util.*;
public abstract class FSM {
    protected List<State> states;
    protected Set<Character> symbols;
    protected State initialState;
    protected Set<State> finalStates;
    protected List<Transition> transitions;

    public abstract void addSymbols(Collection<Character> symbolsToAdd);
    public abstract Set<Character> getSymbols();
    public abstract void addStates(Collection<String> stateNames);
    public abstract List<State> getStates();
    public abstract void setInitialState(String stateName);
    public abstract State getInitialState();
    public abstract void addFinalStates(Collection<String> stateNames);
    public abstract Set<State> getFinalStates();
    public abstract void addTransitions(Collection<Transition> transitionsToAdd);
    public abstract List<Transition> getTransitions();
    public abstract void printConfiguration();
    public abstract void compile(String filename);
    public abstract void load(String filename);
    public abstract String execute(String input);

}
