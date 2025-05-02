import java.util.*;
import java.io.*;

public class DeterministicFSM extends FSM {
    private PrintWriter logWriter;
    private boolean loggingEnabled;

    public DeterministicFSM() {
        states = new ArrayList<>();
        symbols = new LinkedHashSet<>();
        finalStates = new LinkedHashSet<>();
        transitions = new ArrayList<>();
        loggingEnabled = false;
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
            String name = s.toUpperCase();
            State st = new ConcreteState(name);
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
    @Override
    public State getInitialState() { return initialState; }

    @Override
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
    @Override
    public Set<State> getFinalStates() { return finalStates; }

    @Override
    public void addTransitions(Collection<Transition> transitionsToAdd) {
        for (Transition t : transitionsToAdd) {
            transitions.removeIf(existing -> existing.getSymbol() == t.getSymbol()
                    && existing.getCurrentState().equals(t.getCurrentState()));
            transitions.add(t);
        }
    }
    @Override
    public List<Transition> getTransitions() { return transitions; }

    @Override
    public void printConfiguration() {
        System.out.println("SYMBOLS " + symbols);
        System.out.println("STATES " + states.stream().map(State::getName).toList());
        System.out.println("INITIAL STATE " + (initialState != null ? initialState.getName() : ""));
        System.out.println("FINAL STATES " + finalStates.stream().map(State::getName).toList());
        System.out.println("TRANSITIONS");
        for (Transition t : transitions) {
            System.out.println(t.getSymbol() + " " + t.getCurrentState().getName() + " " + t.getNextState().getName());
        }
    }

    @Override
    public void compile(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            // placeholder for binary serialization
        } catch (IOException e) {
            System.out.println("Error: cannot create file " + filename);
        }
    }

    @Override
    public void load(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNum = 0;
            StringBuilder cmdBuilder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                lineNum++;
                String before = line.split(";", 2)[0];
                if (line.contains(";")) {
                    cmdBuilder.append(" ").append(before.trim());
                    String command = cmdBuilder.toString().trim();
                    cmdBuilder.setLength(0);
                    if (!command.isEmpty()) executeCommand(command, lineNum);
                } else {
                    cmdBuilder.append(" ").append(before.trim());
                }
            }
        } catch (IOException e) {
            System.out.println("Error: cannot open file " + filename);
        }
    }

    private void executeCommand(String command, int lineNum) {
        String[] tokens = command.trim().split("\\s+");
        if (tokens.length == 0) return;
        String cmd = tokens[0].toUpperCase();
        try {
            switch (cmd) {
                case "LOG" -> {
                    if (tokens.length >= 2) {
                        String fileToLog = tokens[1];
                        if (loggingEnabled && logWriter != null) logWriter.close();
                        try {
                            logWriter = new PrintWriter(new FileWriter(fileToLog, false));
                            loggingEnabled = true;
                        } catch (IOException e) {
                            System.out.println("Error: cannot create log file " + fileToLog);
                        }
                    } else {
                        if (loggingEnabled && logWriter != null) {
                            logWriter.close();
                            loggingEnabled = false;
                            System.out.println("STOPPED LOGGING");
                        } else {
                            System.out.println("LOGGING was not enabled");
                        }
                    }
                }
                case "SYMBOLS" -> {
                    if (tokens.length >= 2) {
                        List<Character> syms = new ArrayList<>();
                        for (int i = 1; i < tokens.length; i++)
                            for (char c : tokens[i].toCharArray()) syms.add(c);
                        addSymbols(syms);
                    } else System.out.println(getSymbols());
                }
                case "STATES" -> {
                    if (tokens.length >= 2) addStates(Arrays.asList(Arrays.copyOfRange(tokens, 1, tokens.length)));
                    else System.out.println(getStates().stream().map(State::getName).toList());
                }
                case "INITIAL-STATE" -> {
                    if (tokens.length >= 2) setInitialState(tokens[1]);
                    else System.out.println("Error: INITIAL-STATE requires a state name");
                }
                case "FINAL-STATES" -> {
                    if (tokens.length >= 2) addFinalStates(Arrays.asList(Arrays.copyOfRange(tokens, 1, tokens.length)));
                    else System.out.println("Error: FINAL-STATES requires at least one state");
                }
                case "TRANSITIONS" -> {
                    String body = command.substring(cmd.length()).trim();
                    String[] parts = body.split(",");
                    List<Transition> list = new ArrayList<>();
                    for (String part : parts) {
                        String p = part.trim();
                        if (p.isEmpty()) continue;
                        String[] elems = p.split("\\s+");
                        if (elems.length < 3) {
                            System.out.println("Line " + lineNum + ": invalid transition \"" + p + "\"");
                            continue;
                        }
                        char symbol = elems[0].charAt(0);
                        State from = new ConcreteState(elems[1]);
                        State to = new ConcreteState(elems[2]);
                        list.add(new ConcreteTransition(symbol, from, to));
                    }
                    addTransitions(list);
                }
                case "PRINT" -> printConfiguration();
                case "EXECUTE" -> {
                    if (tokens.length >= 2) System.out.println(execute(tokens[1]));
                    else System.out.println("Error: EXECUTE requires an input string");
                }
                case "COMPILE" -> {
                    if (tokens.length >= 2) compile(tokens[1]);
                    else System.out.println("Error: COMPILE requires a filename");
                }
                case "CLEAR" -> {
                    symbols.clear();
                    states.clear();
                    transitions.clear();
                    finalStates.clear();
                }
                case "LOAD" -> {
                    if (tokens.length >= 2) load(tokens[1]);
                    else System.out.println("Error: LOAD requires a filename");
                }
                case "EXIT" -> {
                    System.out.println("TERMINATED BY USER");
                    System.exit(0);
                }
                default -> System.out.println("Line " + lineNum + ": invalid command \"" + cmd + "\"");
            }
        } catch (Exception e) {
            System.out.println("Line " + lineNum + ": " + e.getMessage());
        }
    }

    @Override
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