import java.util.*;
import java.util.stream.Collectors;
import java.io.*;

public class DeterministicFSM extends FSM {
    private static final long serialVersionUID = 1L;
    private transient final Logger logger = new Logger();

    public DeterministicFSM() {
        states = new ArrayList<>();
        symbols = new LinkedHashSet<>();
        finalStates = new LinkedHashSet<>();
        transitions = new ArrayList<>();
    }

    @Override
    public void addSymbols(Collection<Character> symbolsToAdd) {
        for (char c : symbolsToAdd) {
            char u = Character.toUpperCase(c);
            if (Character.isLetterOrDigit(u)) symbols.add(u);
        }
    }

    @Override
    public Set<Character> getSymbols() { return symbols; }

    @Override
    public void addStates(Collection<String> stateNames) {
        for (String s : stateNames) {
            State st = new ConcreteState(s);
            if (!states.contains(st)) {
                states.add(st);
                if (initialState == null) initialState = st;
            }
        }
    }

    @Override
    public List<State> getStates() { return states; }

    @Override
    public void setInitialState(String stateName) {
        State st = states.stream()
                .filter(s -> s.getName().equalsIgnoreCase(stateName))
                .findFirst()
                .orElse(null);
        if (st == null) {
            st = new ConcreteState(stateName);
            states.add(st);
        }
        initialState = st;
    }

    @Override
    public State getInitialState() { return initialState; }

    @Override
    public void addFinalStates(Collection<String> stateNames) {
        for (String s : stateNames) {
            State st = states.stream()
                    .filter(x -> x.getName().equalsIgnoreCase(s))
                    .findFirst()
                    .orElse(null);
            if (st == null) {
                st = new ConcreteState(s);
                states.add(st);
            }
            finalStates.add(st);
        }
    }

    @Override
    public Set<State> getFinalStates() { return finalStates; }

    @Override
    public void addTransitions(Collection<Transition> transitionsToAdd) {
        for (Transition t : transitionsToAdd) {
            transitions.removeIf(existing ->
                    existing.getSymbol() == t.getSymbol() &&
                            existing.getCurrentState().equals(t.getCurrentState()));
            transitions.add(t);
        }
    }

    @Override
    public List<Transition> getTransitions() { return transitions; }

    @Override
    public void printConfiguration() {
        System.out.println("SYMBOLS [" + symbols.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ")) + "]");

        List<State> sortedStates = states.stream()
                .sorted(Comparator.comparing(State::getName))
                .collect(Collectors.toList());
        System.out.println("STATES [" + sortedStates.stream()
                .map(State::getName)
                .collect(Collectors.joining(", ")) + "]");

        System.out.println("INITIAL STATE " + (initialState != null ? initialState.getName() : ""));


        System.out.println("FINAL STATES [" + finalStates.stream()
                .map(State::getName)
                .collect(Collectors.joining(", ")) + "]");

        List<Transition> sortedTransitions = transitions.stream()
                .sorted(Comparator.comparing((Transition t) -> t.getSymbol())
                        .thenComparing(t -> t.getCurrentState().getName()))
                .collect(Collectors.toList());
        System.out.println("TRANSITIONS");
        sortedTransitions.forEach(t -> {
            System.out.println(t.getSymbol() + " " + t.getCurrentState().getName()
                    + " " + t.getNextState().getName());
        });
    }

    @Override
    public void compile(String filename) {
        try {
            new FileManager().saveToBinary(this, filename);
            printAndLog("FSM compiled successfully to file: " + filename);
        } catch (Exception e) {
            printAndLog("Error compiling FSM: " + e.getMessage());
        }
    }

    @Override
    public void load(String filename) {
        try {
            if (filename.toLowerCase().endsWith(".bin") || filename.toLowerCase().endsWith(".fsm")) {
                FSM loaded = new FileManager().loadFromBinary(filename);
                if (loaded instanceof DeterministicFSM) {
                    DeterministicFSM d = (DeterministicFSM) loaded;
                    this.states = d.states;
                    this.symbols = d.symbols;
                    this.initialState = d.initialState;
                    this.finalStates = d.finalStates;
                    this.transitions = d.transitions;
                    printAndLog("FSM loaded successfully from binary file: " + filename);
                } else {
                    printAndLog("Error: Binary file does not contain a valid DeterministicFSM.");
                }
            } else {
                new FileManager().loadFromText(this, filename);
                printAndLog("Commands loaded successfully from text file: " + filename);
            }
        } catch (Exception e) {
            printAndLog("Error loading file " + filename + ": " + e.getMessage());
        }
    }

    public void enableLogging(String filename) {
        logger.startLogging(filename);
    }

    public void disableLogging() {
        logger.stopLogging();
    }

    public void processRawCommand(String command, int lineNum) {
        String response = "";
        executeCommand(command, lineNum);
        logger.log(command);
        if (!response.isEmpty()) logger.log(response);
    }

    private void executeCommand(String command, int lineNum) {
        String[] tokens = command.trim().split("\\s+");
        if (tokens.length == 0) return;
        String cmd = tokens[0].toUpperCase();

        try {
            switch (cmd) {
                case "LOG" -> {
                    if (tokens.length >= 2) enableLogging(tokens[1]);
                    else {
                        if (logger.isEnabled()) {
                            disableLogging();
                            printAndLog("STOPPED LOGGING");
                        } else {
                            printAndLog("LOGGING was not enabled");
                        }
                    }
                }
                case "SYMBOLS" -> {
                    if (tokens.length > 1) {
                        List<String> invalids = new ArrayList<>();
                        for (int i = 1; i < tokens.length; i++) {
                            String tok = tokens[i];
                            if (tok.length() != 1 || !Character.isLetterOrDigit(tok.charAt(0))) {
                                invalids.add(tok);
                            } else {
                                char c = Character.toUpperCase(tok.charAt(0));
                                if (symbols.contains(c)) {
                                    printAndLog("Warning " + tok + " was already declared as a symbol");
                                } else {
                                    symbols.add(c);
                                }
                            }
                        }
                        if (!invalids.isEmpty()) {
                            printAndLog("Warning: invalid symbols " + String.join(", ", invalids));
                        }
                    } else {
                        String list = symbols.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", "));
                        printAndLog(list);
                    }
                }
                case "STATES" -> {
                    if (tokens.length > 1) {
                        for (int i = 1; i < tokens.length; i++) {
                            String name = tokens[i].replaceAll(";+$", "");
                            boolean exists = states.stream()
                                    .anyMatch(s -> s.getName().equalsIgnoreCase(name));
                            if (exists) {
                                printAndLog("Warning: " + name.toUpperCase() + " was already declared as a state");
                            } else {
                                addStates(Collections.singletonList(name));
                            }
                        }
                    } else {
                        String list = states.stream()
                                .map(State::getName)
                                .collect(Collectors.joining(", "));
                        printAndLog(list);
                    }
                }
                case "INITIAL-STATE" -> {
                    if (tokens.length >= 2) {
                        String name = tokens[1].replaceAll(";+$", "");
                        boolean existed = states.stream()
                                .anyMatch(s -> s.getName().equalsIgnoreCase(name));
                        if (!existed) {
                            printAndLog("Warning: " + name.toUpperCase() + " was not previously declared as a state");
                        }
                        setInitialState(name);
                    } else {
                        printAndLog("Error: INITIAL-STATE requires a state name");
                    }
                }
                case "FINAL-STATES" -> {
                    if (tokens.length >= 2) {
                        List<String> names = new ArrayList<>();
                        for (int i = 1; i < tokens.length; i++) {
                            String name = tokens[i].replaceAll(";+$", "");
                            boolean existed = states.stream()
                                    .anyMatch(s -> s.getName().equalsIgnoreCase(name));
                            if (!existed) {
                                printAndLog("Warning: " + name.toUpperCase() + " was not previously declared as a state");
                            }
                            names.add(name);
                        }
                        addFinalStates(names);
                    } else {
                        printAndLog("Error: FINAL-STATES requires at least one state");
                    }
                }
                case "TRANSITIONS" -> {
                    String body = command.substring(cmd.length()).trim();
                    String[] parts = body.split(",");
                    List<Transition> list = new ArrayList<>();

                    for (String p : parts) {
                        String[] e = p.trim().split("\\s+");

                        boolean hasError = false;

                        if (e.length < 3) {
                            printAndLog("Line " + lineNum + ": invalid transition format → \"" + p.trim() + "\"");
                            continue;
                        }

                        char symbol = Character.toUpperCase(e[0].charAt(0));
                        String currentStateName = e[1];
                        String nextStateName = e[2];

                        if (!symbols.contains(symbol)) {
                            printAndLog("Error: invalid symbol A" + symbol);
                            hasError = true;
                        }

                        State currentState = states.stream()
                                .filter(s -> s.getName().equalsIgnoreCase(currentStateName))
                                .findFirst()
                                .orElse(null);
                        if (currentState == null) {
                            printAndLog("Error: invalid state B" + currentStateName);
                            hasError = true;
                        }

                        State nextState = states.stream()
                                .filter(s -> s.getName().equalsIgnoreCase(nextStateName))
                                .findFirst()
                                .orElse(null);
                        if (nextState == null) {
                            printAndLog("Error: invalid state C" + nextStateName);
                            hasError = true;
                        }

                        boolean alreadyExists = transitions.stream().anyMatch(t ->
                                t.getSymbol() == symbol && t.getCurrentState().equals(currentState) &&
                                        !t.getNextState().equals(nextState));
                        if (!hasError && alreadyExists) {
                            printAndLog("Warning: transition already exists for <" + symbol + "," + currentStateName + ">, overridden.");
                        }

                        if (!hasError && currentState != null && nextState != null) {
                            list.add(new ConcreteTransition(symbol, currentState, nextState));
                        }
                    }

                    addTransitions(list);
                }

                case "PRINT" -> {
                    if (tokens.length >= 2) {
                        String filename = tokens[1].replaceAll(";+$", "");
                        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                            writer.println("SYMBOLS " + symbols);
                            writer.println("STATES " + states.stream()
                                    .map(State::getName)
                                    .collect(Collectors.toList()));
                            writer.println("INITIAL STATE " + (initialState != null ? initialState.getName() : ""));
                            writer.println("FINAL STATES " + finalStates.stream()
                                    .map(State::getName)
                                    .collect(Collectors.toList()));
                            writer.println("TRANSITIONS");
                            for (Transition t : transitions) {
                                writer.println(t.getSymbol() + " " +
                                        t.getCurrentState().getName() + " " +
                                        t.getNextState().getName());
                            }
                            printAndLog("FSM configuration written to file: " + filename);
                        } catch (IOException e) {
                            printAndLog("Error: cannot create or override file " + filename + " - " + e.getMessage());
                        }
                    } else {
                        printConfiguration();
                    }
                }
                case "EXECUTE" -> {
                    if (tokens.length >= 2)
                        printAndLog(execute(tokens[1]));
                    else
                        printAndLog("Error: EXECUTE requires an input string");
                }
                case "COMPILE" -> {
                    if (tokens.length >= 2)
                        compile(tokens[1]);
                    else
                        printAndLog("Error: COMPILE requires a filename");
                }
                case "CLEAR" -> {
                    symbols.clear();
                    states.clear();
                    transitions.clear();
                    finalStates.clear();
                    //initialState = null;
                }
                case "LOAD" -> {
                    if (tokens.length >= 2)
                        load(tokens[1]);
                    else
                        printAndLog("Error: LOAD requires a filename");
                }
                case "EXIT" -> {
                    disableLogging();
                    printAndLog("TERMINATED BY USER");
                    System.exit(0);
                }
                default -> printAndLog("Line " + lineNum + ": invalid command \"" + cmd + "\"");
            }
        } catch (Exception e) {
            printAndLog("Line " + lineNum + ": " + e.getMessage());
        }
    }

    private void printAndLog(String message) {
        System.out.println(message);
        logger.log(message);
    }

    @Override
    public String execute(String input) {
        StringBuilder sb = new StringBuilder();
        State current = initialState;
        sb.append(current.getName());
        for (char c : input.toCharArray()) {
            char u = Character.toUpperCase(c);
            if (!symbols.contains(u))
                return "Error: invalid symbol " + c;
            Transition found = null;
            for (Transition t : transitions)
                if (t.getSymbol() == u && t.getCurrentState().equals(current)) {
                    found = t;
                    break;
                }
            if (found == null)
                return "Error: no transition for " + c + " in state " + current.getName();
            current = found.getNextState();
            sb.append(" ").append(current.getName());
        }
        sb.append(finalStates.contains(current) ? " YES" : " NO");
        return sb.toString();
    }
}
