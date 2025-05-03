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
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d, yyyy, HH:mm", Locale.ENGLISH);
        System.out.println("FSM DESIGNER " + versionNo +" "+ fmt.format(now));

        Scanner scanner = new Scanner(System.in);
        StringBuilder cmdBuffer = new StringBuilder();
        System.out.print("? ");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.trim().startsWith(";")) {
                System.out.print("? ");
                continue;
            }
            String beforeSemicolon = line.split(";", 2)[0];
            if (line.contains(";")) {
                cmdBuffer.append(" ").append(beforeSemicolon.trim());
                String command = cmdBuffer.toString().trim();
                cmdBuffer.setLength(0);
                if (!command.isEmpty()) {
                    processor.processCommand(command, 0);
                }
                System.out.print("? ");
            } else {
                cmdBuffer.append(" ").append(beforeSemicolon.trim());
            }
        }
        scanner.close();
    }
}