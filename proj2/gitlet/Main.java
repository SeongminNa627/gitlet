package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                Repository.initCmd();
                Repository.saveHEAD();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                Repository.retrieveHEAD();
                Repository.addStageCmd(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                // TODO: handle the 'commit' command
                // TODO: commiting all the files staged in the staging area.
                Repository.retrieveHEAD();
                Repository.commitCmd(args[1]);
                break;
            case "rm":
                Repository.retrieveHEAD();
                Repository.rmStageCmd(args[1]);
                break;
            case "checkout":
                Repository.retrieveHEAD();
                if (args.length == 2) {
                    Repository.checkoutBranchCmd(args[1]);
                }
                else if (args.length == 3) {
                    Repository.checkoutHEADFileCmd(args[2]);
                }
                else if (args.length == 4) {
                    Commit theCommit = Repository.getCommit(args[1]);
                    Repository.checkoutCommitFileCmd(theCommit, args[3]);
                }
                break;
            case "log":
                Repository.retrieveHEAD();
                Repository.logCmd();
                break;
            case "global-log":
                Repository.retrieveHEAD();
                Repository.globalLogCmd();
                break;
            case "branch":
                Repository.saveBranch(args[1]);
                Repository.createBranch();
                break;
            case "merge":
                Repository.retrieveHEAD();
                Repository.mergeCmd(args[1]);
                break;
            case "find":
                Repository.retrieveHEAD();
                Repository.findCmd(args[1]);
            case "statusCmd":
                Repository.retrieveHEAD();
                Repository.statusCmd();
            case "rm-branch":
                Repository.retrieveHEAD();
                Repository.rmBranchCmd(args[1]);
            case "reset":
                Repository.retrieveHEAD();
                Repository.resetCmd(args[1]);
        }
    }
}
