import java.io.BufferedReader;
import java.io.InputStreamReader;

public class VersionUtil {

    public static String getVersion() {
        try {
            Process process = Runtime.getRuntime().exec("git rev-parse --short HEAD");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();
            reader.close();

            if (version != null && !version.isBlank()) {
                return version.trim();
            } else {
                return "UNKNOWN";
            }
        } catch (Exception e) {
            System.err.println("Error while getting version: " + e.getMessage());
            return "UNKNOWN";
        }
    }
}

