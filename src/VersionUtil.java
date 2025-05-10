import java.io.BufferedReader;
import java.io.InputStreamReader;

public class VersionUtil {

    public static String getLatestGitTagVersion() {
        try {
            Process process = Runtime.getRuntime().exec("git describe --tags --abbrev=0");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();
            reader.close();

            return (version != null && !version.isBlank()) ? version.trim() : "NO TAG FOUND";
        } catch (Exception e) {
            System.err.println("Error getting release version: " + e.getMessage());
            return "UNKNOWN";
        }
    }
}
