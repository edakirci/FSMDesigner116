import java.util.*;
public class DeterministicFSM extends FSM{
    public DeterministicFSM() {
        states = new ArrayList<>();
        symbols = new LinkedHashSet<>();
        finalStates = new LinkedHashSet<>();
        transitions = new ArrayList<>();
    }

    public void addSymbols(Collection<Character> symbolsToAdd) {
        for (char c : symbolsToAdd) {
            char u = Character.toUpperCase(c);
            if (Character.isLetterOrDigit(u)) symbols.add(u);
        }
    }
    public Set<Character> getSymbols() { return symbols; }

    public void addStates(Collection<String> stateNames) {
        for (String s : stateNames) {
            String name = s.toUpperCase();
            State st = new ConcreteState(name);
            if (!states.contains(st)) {
                states.add(st);
                if (initialState == null) initialState = st;
            }
        }
    }
    public List<State> getStates() { return states; }

    public void setInitialState(String stateName) {
        String name = stateName.toUpperCase();
        State st = states.stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
        if (st == null) {
            st = new ConcreteState(name);
            states.add(st);
        }
        initialState = st;
    }
    public State getInitialState() { return initialState; }

    public void addFinalStates(Collection<String> stateNames) {
        for (String s : stateNames) {
            String name = s.toUpperCase();
            State st = states.stream()
                    .filter(x -> x.getName().equalsIgnoreCase(name))
                    .findFirst().orElse(null);
            if (st == null) {
                st = new ConcreteState(name);
                states.add(st);
            }
            finalStates.add(st);
        }
    }
    public Set<State> getFinalStates() { return finalStates; }

    public void addTransitions(Collection<Transition> transitionsToAdd) {
        for (Transition t : transitionsToAdd) {
            transitions.removeIf(existing -> existing.getSymbol() == t.getSymbol()
                    && existing.getCurrentState().equals(t.getCurrentState()));
            transitions.add(t);
        }
    }
    public List<Transition> getTransitions() { return transitions; }

    public void printConfiguration() {
        System.out.println("Symbols: " + symbols);
        System.out.println("States: " + getStates().stream().map(State::getName).toList());
        System.out.println("Initial: " + initialState.getName());
        System.out.println("Final: " + finalStates.stream().map(State::getName).toList());
        System.out.println("Transitions:");
        for (Transition t : transitions) {
            System.out.println(t.getSymbol()+" "+t.getCurrentState().getName()+" -> "+t.getNextState().getName());
        }
    }

    public void compile(String filename) { }
    public void load(String filename) { }

    public String execute(String input) {
        StringBuilder sb = new StringBuilder();
        State current = initialState;
        sb.append(current.getName());
        for (char c : input.toCharArray()) {
            char u = Character.toUpperCase(c);
            if (!symbols.contains(u)) return "Error: invalid symbol " + c;
            Transition found = null;
            for (Transition t : transitions) {
                if (t.getSymbol() == u && t.getCurrentState().equals(current)) {
                    found = t;
                    break;
                }
            }
            if (found == null) return "Error: no transition for " + c + " in state " + current.getName();
            current = found.getNextState();
            sb.append(" ").append(current.getName());
        }
        sb.append(finalStates.contains(current) ? " YES" : " NO");
        return sb.toString();
    }
}

