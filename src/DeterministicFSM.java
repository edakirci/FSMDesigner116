import java.util.*;
import java.util.stream.Collectors;

public class DeterministicFSM extends FSM {
    private final Logger logger = new Logger();

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
        State st = states.stream().filter(s -> s.getName().equalsIgnoreCase(stateName)).findFirst().orElse(null);
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
            State st = states.stream().filter(x -> x.getName().equalsIgnoreCase(s)).findFirst().orElse(null);
            if (st == null) { st = new ConcreteState(s); states.add(st); }
            finalStates.add(st);
        }
    }
    @Override
    public Set<State> getFinalStates() { return finalStates; }

    @Override
    public void addTransitions(Collection<Transition> transitionsToAdd) {
        for (Transition t : transitionsToAdd) {
            transitions.removeIf(existing -> existing.getSymbol() == t.getSymbol() && existing.getCurrentState().equals(t.getCurrentState()));
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
        new FileManager().saveToBinary(this, filename);
    }

    @Override
    public void load(String filename) {
        new FileManager().loadFromText(this, filename);
    }

    public void enableLogging(String filename) { logger.startLogging(filename); }
    public void disableLogging()             { logger.stopLogging();       }

    public void processRawCommand(String command, int lineNum) {
        String response = ""; // optional: capture response
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
                    if (tokens.length >= 2) enableLogging(tokens[1]); else disableLogging();
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
                                    System.out.println("Warning " + tok + " was already declared as a symbol");
                                } else {
                                    symbols.add(c);
                                }
                            }
                        }
                        if (!invalids.isEmpty()) {
                            System.out.println("Warning: invalid symbols " + String.join(", ", invalids));
                        }
                    } else {
                        String list = symbols.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", "));
                        System.out.println(list);
                    }
                }
                case "STATES" -> {
                    if (tokens.length>=2) addStates(Arrays.asList(Arrays.copyOfRange(tokens,1,tokens.length)));
                    else System.out.println(getStates().stream().map(State::getName).toList());
                }
                case "INITIAL-STATE" -> {
                    if(tokens.length>=2) setInitialState(tokens[1]); else System.out.println("Error: INITIAL-STATE requires a state name");
                }
                case "FINAL-STATES" -> {
                    if(tokens.length>=2) addFinalStates(Arrays.asList(Arrays.copyOfRange(tokens,1,tokens.length)));
                    else System.out.println("Error: FINAL-STATES requires at least one state");
                }
                case "TRANSITIONS" -> {
                    String body = command.substring(cmd.length()).trim();
                    String[] parts = body.split(",");
                    List<Transition> list = new ArrayList<>();
                    for(String p:parts) {
                        String[] e = p.trim().split("\\s+");
                        if(e.length<3) System.out.println("Line " + lineNum + ": invalid transition \""+p.trim()+"\"");
                        else list.add(new ConcreteTransition(e[0].charAt(0), new ConcreteState(e[1]), new ConcreteState(e[2])));
                    }
                    addTransitions(list);
                }
                case "PRINT" -> printConfiguration();
                case "EXECUTE" -> {
                    if(tokens.length>=2) System.out.println(execute(tokens[1]));
                    else System.out.println("Error: EXECUTE requires an input string");
                }
                case "COMPILE" -> {
                    if(tokens.length>=2) compile(tokens[1]); else System.out.println("Error: COMPILE requires a filename");
                }
                case "CLEAR" -> { symbols.clear(); states.clear(); transitions.clear(); finalStates.clear(); initialState=null; }
                case "LOAD" -> {
                    if(tokens.length>=2) load(tokens[1]); else System.out.println("Error: LOAD requires a filename");
                }
                case "EXIT" -> { disableLogging(); System.out.println("TERMINATED BY USER"); System.exit(0); }
                default -> System.out.println("Line " + lineNum + ": invalid command \"" + cmd + "\"");
            }
        } catch(Exception e) {
            System.out.println("Line " + lineNum + ": " + e.getMessage());
        }
    }

    @Override
    public String execute(String input) {
        StringBuilder sb = new StringBuilder();
        State current = initialState;
        sb.append(current.getName());
        for(char c: input.toCharArray()) {
            char u = Character.toUpperCase(c);
            if(!symbols.contains(u)) return "Error: invalid symbol " + c;
            Transition found=null;
            for(Transition t:transitions) if(t.getSymbol()==u && t.getCurrentState().equals(current)) { found=t; break; }
            if(found==null) return "Error: no transition for " + c + " in state " + current.getName();
            current = found.getNextState();
            sb.append(" ").append(current.getName());
        }
        sb.append(finalStates.contains(current)?" YES":" NO");
        return sb.toString();
    }
}