package gitlet;
import java.util.*;
public class CommAncesFinder {
    // we maintain a list of bfs of HEAD, and for the other branch, we check every node in the other branch in bfs order to find the latest common ancestors.
    static Commit HEAD;
    static ArrayList<Commit> HEADFringe = new ArrayList(5);
    static Commit theOther;
    static ArrayList<Commit> theOtherFringe = new ArrayList(5);
    static HashSet<String> MarkedCommits = new HashSet<>();

    public CommAncesFinder(){
        HEAD = null;
        theOther  = null;
    }

    public CommAncesFinder(Commit c1, Commit c2){
        HEAD = c1;
        theOther = c2;

    }

    public static String find(){
        bfs(HEAD);
        MarkedCommits.clear();
        int pos = 0;
        Commit p = theOther;
        theOtherFringe.add(p);
        while (!(pos > theOtherFringe.size())){
            Commit currComm = theOtherFringe.get(pos);
            if (!MarkedCommits.contains(currComm)) {
                if (HEADFringe.contains(currComm)) {
                    return currComm.getID();
                }
                if (!(currComm.getParent() == null)) {
                    theOtherFringe.add(Repository.getCommit(currComm.getParent()));
                }
                if (!(currComm.getOtherParent() == null)) {
                    theOtherFringe.add(Repository.getCommit(currComm.getOtherParent()));
                }
            }
            pos ++;
            p = theOtherFringe.get(pos);
        }
        return null;
    }

    public static void bfs(Commit c) {
        Commit p = c;
        HEADFringe.add(p);
        int pos = 0;
        while (!(pos > HEADFringe.size())){
            Commit currComm = HEADFringe.get(pos);
            if (!MarkedCommits.contains(currComm)){
                if (!(currComm.getParent() == null)) {
                    HEADFringe.add(Repository.getCommit(currComm.getParent()));
                }
                if (!(currComm.getOtherParent() == null)){
                    HEADFringe.add(Repository.getCommit(currComm.getOtherParent()));
                }
            }
            pos ++;
            p = HEADFringe.get(pos);
        }
    }
}

