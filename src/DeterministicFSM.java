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

        System.out.println("INITIAL STATE [" + (initialState != null ? initialState.getName() : "") + "]");

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
                            String tok = tokens[i].replaceAll(";+$", ""); // Noktalı virgül temizle

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
                        printAndLog("SYMBOLS [" + list + "]");
                    }
                }




                case "STATES" -> {
                    if (tokens.length > 1) {
                        // Yeni state ekleme modu
                        for (int i = 1; i < tokens.length; i++) {
                            // sondaki ',' veya ';' kırp
                            String tok = tokens[i].replaceAll("[,;]+$", "").trim();
                            if (tok.isEmpty()) continue;

                            // yalnızca alfanümerik dizi kabul et
                            if (!tok.matches("[A-Za-z0-9]+")) {
                                printAndLog("Error: invalid state " + tok);
                                continue;
                            }

                            String upper = tok.toUpperCase();
                            boolean exists = states.stream()
                                    .anyMatch(s -> s.getName().equalsIgnoreCase(upper));
                            if (exists) {
                                printAndLog("Warning: " + upper + " was already declared as a state");
                            } else {
                                addStates(Collections.singletonList(tok));
                            }
                        }
                    } else {
                        // Listeleme modu: yalnızca geçerli state’ler, tek virgül + boşluk
                        String list = states.stream()
                                .map(State::getName)
                                .collect(Collectors.joining(", "));
                        printAndLog(list);
                    }
                }



                case "INITIAL-STATE" -> {
                    if (tokens.length >= 2) {
                        // sondaki ';' veya ',' karakterlerini kaldır
                        String tok = tokens[1].replaceAll("[,;]+$", "").trim();

                        // mutlaka tamamen alfanümerik olmalı
                        if (!tok.matches("[A-Za-z0-9]+")) {
                            printAndLog("Error: invalid state " + tok);
                        } else {
                            String upper = tok.toUpperCase();
                            boolean existed = states.stream()
                                    .anyMatch(s -> s.getName().equalsIgnoreCase(upper));
                            if (!existed) {
                                printAndLog("Warning: " + upper + " was not previously declared as a state");
                            }
                            // geçerli ve temiz isimle initialState'i güncelle
                            setInitialState(upper);
                        }
                    } else {
                        printAndLog("Error: INITIAL-STATE requires a state name");
                    }
                }

                case "FINAL-STATES" -> {
                    if (tokens.length >= 2) {
                        for (int i = 1; i < tokens.length; i++) {
                            // sondaki ',' veya ';' karakterlerini kaldır
                            String tok = tokens[i].replaceAll("[,;]+$", "").trim();

                            // yalnızca tamamen alfanümerik dizgeler kabul edilecek
                            if (!tok.matches("[A-Za-z0-9]+")) {
                                printAndLog("Error: invalid state " + tok);
                                continue;
                            }

                            String upper = tok.toUpperCase();

                            // Eğer daha önce tüm states içinde yoksa, uyar ve ekle
                            boolean existedInStates = states.stream()
                                    .anyMatch(s -> s.getName().equalsIgnoreCase(upper));
                            if (!existedInStates) {
                                printAndLog("Warning: " + upper + " was not previously declared as a state");
                                addStates(Collections.singletonList(upper));
                            }

                            // Şimdi finalStates içinde zaten varsa uyar, yoksa ekle
                            State st = states.stream()
                                    .filter(s -> s.getName().equalsIgnoreCase(upper))
                                    .findFirst().orElseThrow();
                            if (finalStates.contains(st)) {
                                printAndLog("Warning: " + upper + " was already declared as a final state");
                            } else {
                                finalStates.add(st);
                            }
                        }
                    } else {
                        printAndLog("Error: FINAL-STATES requires at least one state name");
                    }
                }


                case "TRANSITIONS" -> {
                    String body = command.substring(cmd.length()).trim();

                    if (body.endsWith(";")) {
                        body = body.substring(0, body.length() - 1).trim();
                    }
                    if (body.isEmpty()) break;

                    String[] parts = body.split(",");
                    List<Transition> list = new ArrayList<>();

                    for (String part : parts) {
                        String raw = part.trim();
                        if (raw.isEmpty()) continue;

                        String[] e = raw.replaceAll(";+$", "").split("\\s+");

                        if (e.length < 3) {
                            printAndLog("Line " + lineNum + ": invalid transition format → \"" + raw + "\"");
                            continue;
                        } else if (e.length > 3) {
                            printAndLog("Error: comma or semicolon expected");
                            continue;
                        }

                        String symTok = e[0].trim();
                        String curTok = e[1].trim();
                        String nxtTok = e[2].trim();

                        if (symTok.length() != 1 || !Character.isLetterOrDigit(symTok.charAt(0))) {
                            printAndLog("Error: invalid symbol format → \"" + symTok + "\"");
                            continue;
                        }
                        char symbol = Character.toUpperCase(symTok.charAt(0));
                        if (!symbols.contains(symbol)) {
                            printAndLog("Error: invalid symbol " + symbol);
                            continue;
                        }

                        State curState = states.stream()
                                .filter(s -> s.getName().equalsIgnoreCase(curTok)).findFirst().orElse(null);
                        if (curState == null) {
                            printAndLog("Error: invalid state " + curTok);
                            continue;
                        }
                        State nxtState = states.stream()
                                .filter(s -> s.getName().equalsIgnoreCase(nxtTok)).findFirst().orElse(null);
                        if (nxtState == null) {
                            printAndLog("Error: invalid state " + nxtTok);
                            continue;
                        }

                        Iterator<Transition> it = transitions.iterator();
                        while (it.hasNext()) {
                            Transition old = it.next();
                            if (old.getSymbol() == symbol && old.getCurrentState().equals(curState)) {
                                printAndLog(old.getNextState().equals(nxtState)
                                        ? "Warning: transition <" + symbol + "," + curTok + "> already exists with same target."
                                        : "Warning: transition already exists for <" + symbol + "," + curTok + ">, overridden.");
                                it.remove();
                                break;
                            }
                        }
                        list.add(new ConcreteTransition(symbol, curState, nxtState));
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
                    if (tokens.length >= 2) {
                        String filename = tokens[1].replaceAll(";+$", "");

                        // 1) Geçerli uzantı mı? Yalnızca .fsm veya .bin
                        if (!filename.matches("^[A-Za-z0-9._-]+\\.(fsm|bin)$")) {
                            printAndLog("Error: invalid filename " + filename);
                        } else {
                            // 2) Dosyayı yazmayı dene
                            try {
                                new FileManager().saveToBinary(this, filename);
                                printAndLog("FSM compiled successfully to file: " + filename);
                            } catch (IOException e) {
                                // dosya oluşturulamıyorsa anlamlı mesaj
                                printAndLog("Error: cannot create or override file "
                                        + filename + " – The system cannot find the path specified");
                            }
                        }
                    } else {
                        printAndLog("Error: COMPILE requires a filename");
                    }
                }

                case "CLEAR" -> {
                    // If any extra tokens accompany CLEAR, it's an error
                    if (tokens.length > 1) {
                        printAndLog("Error: CLEAR does not take any arguments");
                    } else {
                        symbols.clear();
                        states.clear();
                        transitions.clear();
                        finalStates.clear();
                        initialState = null;
                    }
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