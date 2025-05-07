import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        DeterministicFSM fsm = new DeterministicFSM();
        CommandProcessor processor = new CommandProcessor(fsm);

        if (args.length == 1) {
            fsm.load(args[0]);
            return;
        }

        String versionNo = VersionUtil.getVersion();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fmt = DateTimeFormatter
                .ofPattern("MMMM d, yyyy, HH:mm", Locale.ENGLISH);
        System.out.println("FSM DESIGNER " + versionNo + " " + fmt.format(now));

        Scanner scanner = new Scanner(System.in);
        StringBuilder cmdBuffer = new StringBuilder();
        String prompt = "? ";
        System.out.print(prompt);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            int idx = line.indexOf(';');
            String content = (idx >= 0 ? line.substring(0, idx) : line).trim();


            if (content.isEmpty()) {
                System.out.print(prompt);
                continue;
            }

            cmdBuffer.append(' ').append(content);

            if (idx >= 0) {
                String command = cmdBuffer.toString().trim();
                cmdBuffer.setLength(0);
                if (!command.isEmpty()) {
                    processor.processCommand(command, 0);
                }
                System.out.print(prompt);
            }
        }

        scanner.close();
    }
}
