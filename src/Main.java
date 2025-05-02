import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DeterministicFSM fsm = new DeterministicFSM();
        CommandProcessor processor = new CommandProcessor(fsm);

        if (args.length == 1) {
            fsm.load(args[0]);
            return;
        }

        String versionNo = "1.0";
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d, yyyy, HH:mm");
        System.out.println("FSM DESIGNER " + versionNo + " " + fmt.format(now));

        Scanner scanner = new Scanner(System.in);
        StringBuilder cmdBuffer = new StringBuilder();
        System.out.print("? ");
    }

}
