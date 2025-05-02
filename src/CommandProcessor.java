public class CommandProcessor {
    private final DeterministicFSM fsm;
    private final FileManager fileManager;
    public CommandProcessor(DeterministicFSM fsm) {
        this.fsm = fsm;
        this.fileManager = new FileManager();
    }
    public void processCommand(String command,int lineNum) {
        fsm.processRawCommand(command,lineNum);
    }
}
 