import java.io.*;
public class FileManager {
    public void loadFromText(DeterministicFSM fsm, String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNum = 0;
            StringBuilder cmdBuf = new StringBuilder();

            while ((line = br.readLine()) != null) {
                lineNum++;

                int idx = line.indexOf(';');
                String content = (idx >= 0 ? line.substring(0, idx + 1) : line).trim(); // ✅ +1 eklenerek komutun tamamı alınır

                if (content.isEmpty()) {
                    continue;
                }

                cmdBuf.append(' ').append(content);

                if (idx >= 0) {
                    String cmd = cmdBuf.toString().trim();
                    cmdBuf.setLength(0);
                    if (!cmd.isEmpty()) {
                        fsm.processRawCommand(cmd, lineNum);
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("cannot open file " + filename);
        }
    }


    public void saveToBinary(FSM fsm, String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(fsm);
        }
    }

    public FSM loadFromBinary(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (FSM) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error: cannot deserialize from " + filename);
            return null;
        }
    }

    public void saveToText(FSM fsm, String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.print("SYMBOLS");
            for (char s : fsm.getSymbols()) pw.print(" " + s);
            pw.println(";");

            pw.print("STATES");
            for (State st : fsm.getStates()) pw.print(" " + st.getName());
            pw.println(";");

            pw.println("INITIAL-STATE " + fsm.getInitialState().getName() + ";");

            pw.print("FINAL-STATES");
            for (State st : fsm.getFinalStates()) pw.print(" " + st.getName());
            pw.println(";");

            pw.print("TRANSITIONS");
            for (Transition t : fsm.getTransitions()) {
                pw.print(" " + t.getSymbol() + " " + t.getCurrentState().getName() + " " + t.getNextState().getName() + ",");
            }
            pw.println(";");
        } catch (IOException e) {
            System.out.println("Error: cannot write file " + filename);
        }
    }
}
