import java.io.*;
    public class Logger {
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

        public void stopLogging() {
            if (enabled && writer != null) writer.close();
            enabled = false;
        }

        public void log(String entry) {
            if (enabled && writer != null) {
                writer.println(entry);
                writer.flush();
            }
        }

        public boolean isEnabled() { return enabled; }
    }

