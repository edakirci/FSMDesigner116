import java.io.*;
public class Main {

    class Logger {
        private PrintWriter writer;
        private boolean enabled;

        public void startLogging(String filename) {
            stopLogging();
            try {
                writer = new PrintWriter(new FileWriter(filename, false));
                enabled = true;
            } catch (IOException e) {
                System.out.println("Error: cannot create log file " + filename);
                enabled = false;
            }
        }
    }
}
