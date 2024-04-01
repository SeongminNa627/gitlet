package gitlet;

import java.io.File;
import static gitlet.Utils.*;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Mason Na
 */
public class Repository {
    static Commit branch;
    static Commit HEAD;
    static Commit Master;
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");
    public static final File STAGE_ADD_DIR = join(GITLET_DIR, "staging_add");
    public static final File STAGE_RM_DIR = join(GITLET_DIR, "staging_rm");
    public static final File HEAD_DIR = join(GITLET_DIR, "HEAD");
    public static final File COMMIT_DIR = join(GITLET_DIR, "COMMITS");
    public static final File BRANCHES_DIR = join(GITLET_DIR, "branches");

    public static void initCmd() {
        GITLET_DIR.mkdir();
        BLOB_DIR.mkdirs();
        STAGE_ADD_DIR.mkdirs();
        HEAD_DIR.mkdirs();
        STAGE_RM_DIR.mkdirs();
        COMMIT_DIR.mkdirs();
        BRANCHES_DIR.mkdirs();
        Commit initCommit = new Commit("Initial Commit", null, null, null, null);
        Master = initCommit;
        HEAD = Master;
        saveBranch("master");
    }

    public static void addStageCmd(String fileName){
        addStage(join(CWD, fileName));
    }

    public static void addStage(File file) {
        File givenFile = file;
        File stagedFile = join(STAGE_ADD_DIR, file.getName());
        if (isStaged(stagedFile)) {
            if (!sameContents(givenFile, stagedFile)) {
                //TODO:  staged but has the same contents
                //TODO:  all we need to do is change the content of staged file.
                modifyContents(stagedFile, givenFile);
            } else {
                stagedFile.delete();
            }
        } else if (!HEAD.isTracking(givenFile)) {
            // TODO: It is staged but doesn't have the same contents
            // TODO: we need to stage it first and copy givenFile's contents
            // TODO
            modifyContents(stagedFile, givenFile);
        }
    }

    public static void rmStageCmd(String fileName) {
        rmStage(join(CWD, fileName));
    }

    public static void rmStage(File file){
        /**
         * Unstage the file if it is currently staged for addition.
         * If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so
         * (do not remove it unless it is tracked in the current commit).
         */
        File givenFile = file;
        File stagedFile = join(STAGE_ADD_DIR, file.getName());
        File stageForRm = join(STAGE_RM_DIR, file.getName());
        if (isStaged(stagedFile)) {
            restrictedDelete(stagedFile);
        } else if (HEAD.isTracking(givenFile)) {
            try {
                stageForRm.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            writeContents(stageForRm, readContents(givenFile));
            givenFile.delete();
        } else {
            // throw error with error message saying "No reason to remove the file."
        }
    }

    public static void commitCmd(String msg) {
        //TODO: 0. Basically Identical one to HEAD.
        List<String> namesInAddStage = plainFilenamesIn(STAGE_ADD_DIR);
        List<String> namesInRmStage = plainFilenamesIn(STAGE_RM_DIR);
        //TODO: 1. Clone the head commit.
        Commit newCommit = HEAD.clone();

        //TODO: 2. Modify the timestamp and msg.
        Date time = new Date();
        newCommit.setTimestamp(time);
        newCommit.setMessage(msg);
        //TODO: 3. Modify the blob map according to the staging area.
        if (!(namesInAddStage == null)) {
            for (String f : namesInAddStage) {
                File inStage = join(STAGE_ADD_DIR, f);
                newCommit.putBlobMap(f, sha1(readContents(inStage)));
                inStage.delete();
            }
        }
        if (!(namesInRmStage == null)) {
            for (String f : namesInRmStage) {
                File inRmStage = join(STAGE_RM_DIR, f);
                newCommit.removeFromBlobMap(f);
                inRmStage.delete();
            }
        }
        //TODO: 4. Advance the head pointer to the most recent one.
        //TODO: How would I implement the persistence of Commits once I move the HEAD pointer away.
        File commitFile = join(COMMIT_DIR, sha1(serialize(HEAD)));
        try {
            commitFile.createNewFile();
            writeObject(commitFile, HEAD);
        } catch (IOException e) {
            e.printStackTrace();
        }
        newCommit.setParent(sha1(serialize(HEAD)));
        HEAD = newCommit;
        saveHEAD();
    }

    public static void checkoutHEADFileCmd(String fileName) {
        checkoutCommitFileCmd(HEAD, fileName);
    }

    public static void checkoutCommitFileCmd(Commit c, String fileName) {
        // TODO: 1.Look up the file from the given commit.
        //      If it does exist,
        //          if there is another file with the same exists in CWD
        // TODO:        overWrite what's in the CWD with the file I'm checking out to.
        // TODO:    else{
        // TODO:        createNewFile
        File targetFile = join(CWD, fileName);
        HashMap<String, String> blobMap = c.getBlobMap();
        if (blobMap.containsKey(fileName)) {
            String contentSha1 = blobMap.get(fileName);
            String contents = getContents(contentSha1);
            try {
                if (!targetFile.exists()) {
                    targetFile.createNewFile();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            writeContents(targetFile, contents);
        }
    }

    public static void checkoutBranchCmd(String branchName) {
        File targetBranch = join(BRANCHES_DIR, branchName);
        if (targetBranch.exists()) {
            String commitID = readContentsAsString(targetBranch);
            HEAD = getCommit(commitID);
        }
    }

    public static void saveBranch(String branchName) {
        File newBranch = join(BRANCHES_DIR, branchName);
        try {
            if (!newBranch.exists()) {
                newBranch.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeContents(newBranch, sha1(serialize(HEAD)));
    }

    public static String getBranchID(String branchName) {
        File targetBranch = join(BRANCHES_DIR, branchName);
        if (targetBranch.exists()) {
            return readContentsAsString(targetBranch);
        }
        return null;
    }

    public static void createBranch() {
        branch = HEAD;
    }

    public static void globalLogCmd() {
        for (String commitID : plainFilenamesIn(COMMIT_DIR)) {
            Commit corrCommit = getCommit(commitID);
            System.out.println("==");
            System.out.println("commit " + commitID);
            System.out.println("Date: " + corrCommit.getTimestamp().toString());
            System.out.println(corrCommit.getMessage());
            System.out.println();
        }
    }

    public static void logCmd() {
        Commit p = HEAD;
        while (p.getParent() != null){
            System.out.println("===");
            System.out.println("commit " + p.getID());
            System.out.println("Date: " + p.getTimestamp());
            System.out.println(p.getMessage());
            System.out.println();
            p = getCommit(p.getParent());
        }

    }

    public static void findCmd(String msg) {
        Commit p;
        for (String id: plainFilenamesIn(COMMIT_DIR)){
            p = getCommit(id);
            if (p.getID() == msg){
                System.out.println(p.getID());
            }
        }
    }

    public static void statusCmd(){
        System.out.println("=== Branches ===");
        for (String branchName: plainFilenamesIn(BRANCHES_DIR)){
            File branchFile = join(BRANCHES_DIR, branchName);
            String branchID = readContentsAsString(branchFile);
            Commit p = getCommit(branchID);
            if (HEAD.getID() == p.getID()){
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.println("=== Staged Files ===");
        for (String stagedFileName: plainFilenamesIn(STAGE_ADD_DIR)){
            System.out.println(stagedFileName);
        }
        System.out.println("=== Removed Files ===");
        for (String stagedFileName: plainFilenamesIn(STAGE_RM_DIR)){
            System.out.println(stagedFileName);
        }
        System.out.println("=== Modifications Not Staged For Commit");
        for (String fileName: plainFilenamesIn(CWD)){
            if (HEAD.getBlobMap().containsKey(fileName) && !HEAD.isTracking(join(CWD, fileName))){
                System.out.println(fileName + " (modified)");
            }
            else if (isStaged(join(STAGE_ADD_DIR, fileName)) && !sameContents(join(STAGE_ADD_DIR,fileName), join(CWD, fileName))){
                System.out.println(fileName + " (modified)");
            }

        }
        for (String fileName: plainFilenamesIn(STAGE_ADD_DIR)){
            if (!join(CWD,fileName).exists()){
                System.out.println(fileName + " (deleted)");
            }
        }
        for (String fileName: HEAD.getBlobMap().keySet()){
            if (!join(STAGE_RM_DIR, fileName).exists() && !join(CWD, fileName).exists()){
                System.out.println(fileName + " (deleted)");
            }
        }
//      junk.txt (deleted)
//      wug3.txt (modified)
        System.out.println("=== Untracked Files===");
        for (String cwdFileName: plainFilenamesIn(CWD)){
            if(!HEAD.isTracking(join(CWD, cwdFileName))){
                System.out.println(cwdFileName);
            }
        }
    }

    public static boolean sameContents(File f1, File f2) {
        return (sha1(readContents(f1)).equals(sha1(readContents(f2))));
    }

    public static void modifyContents(File copyToHere, File beingCopied) {
        try {
            if (!copyToHere.exists()) {
                copyToHere.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeContents(copyToHere, readContents(beingCopied));
        // In a case where the contents of that file does exist, you don't create it.
        try {
            File newBlob = join(BLOB_DIR, sha1(readContents(beingCopied)));
            if (!newBlob.exists()) {
                newBlob.createNewFile();
                writeContents(newBlob, readContents(beingCopied));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Commit getCommit(String sha) {
        // This function takes in a sha1 ID as an input and returns the corresponding Commit.
        return readObject(join(COMMIT_DIR, sha), Commit.class);
    }

    public static String getContents(String sha) {
        return readContentsAsString(join(BLOB_DIR, sha));
    }

    public static boolean isStaged(File f) {
        // redundant expression of exists()
        return f.exists();
    }

    public static void rmBranchCmd(String branchName){
        File targetBranch = join(BRANCHES_DIR, branchName);
        if (targetBranch.exists()){
            restrictedDelete(targetBranch);
        }
    }

    public static void resetCmd(String commitID){
        Commit targetCommit = getCommit(commitID);
        for (String fileName: plainFilenamesIn(CWD)){
            if (targetCommit.isTracking(join(CWD,fileName))){
                checkoutCommitFileCmd(targetCommit, fileName);
            }
            if (HEAD.getBlobMap().containsKey(fileName)){
                restrictedDelete(join(STAGE_ADD_DIR, fileName));
                restrictedDelete(join(STAGE_RM_DIR, fileName));
            }
        }
    }

    public static void retrieveHEAD() {
        // this method retrieve HEAD commit from the HEAD file which saves HEAD commit persistently.
        HEAD = readObject(join(HEAD_DIR, "HEAD"), Commit.class);
    }

    public static void saveHEAD() {
        File head = join(HEAD_DIR, "HEAD");
        try {
            if (!head.exists()) {
                head.createNewFile();
            }
        } catch (IOException e) {
            e.getMessage();
        }
        writeObject(head, HEAD);
    }

    public static boolean isModified(String fileName, HashMap inThis, HashMap comparedTo) {
        //TODO: deletion is an example of modification. If they are both deleted, they are modified in the same way.
        return !(inThis.get(fileName) == comparedTo.get(fileName));
    }

    public static void mergeCmd(String branchName) {
        Commit other = getCommit(getBranchID(branchName));
        Commit headCommit = HEAD;
        String splitPointID = new CommAncesFinder(other, headCommit).find();
        Commit splitPoint = getCommit(splitPointID);
        Set<String> otherFileSet = other.getBlobMap().keySet();
        Set<String> headCommitFileSet = headCommit.getBlobMap().keySet();
        Set<String> splitPointFileSet = splitPoint.getBlobMap().keySet();
        // TODO: Major Algorithm Here.
        Set<String> collectiveSet = headCommitFileSet;
        collectiveSet.addAll(otherFileSet);
        collectiveSet.addAll(splitPointFileSet);
        for (String fileName : collectiveSet) {
            if (!splitPointFileSet.contains(fileName)) {
                if (!otherFileSet.contains(fileName)) {
                    //TODO: not in split nor Other but in HEAD --> HEAD
                    File tobeAdded = join(STAGE_ADD_DIR, fileName);
                    String contentsID = headCommit.getBlobMap().get(fileName);
                    writeContents(tobeAdded, getContents(contentsID));
                    addStage(tobeAdded);
                }
                else if (!headCommitFileSet.contains(fileName)) {
                    //TODO: not in split nor HEAD but in Other --> Other
                    File tobeAdded = join(STAGE_ADD_DIR, fileName);
                    String contentsID = other.getBlobMap().get(fileName);
                    writeContents(tobeAdded, getContents(contentsID));
                    addStage(tobeAdded);
                }
            }
            else {
               if (isModified(fileName, splitPoint.getBlobMap(), other.getBlobMap())){
                   if (!isModified(fileName, splitPoint.getBlobMap(), headCommit.getBlobMap())) {
                       //TODO: modified in Other but not HEAD --> Other
                       File tobeAdded = join(STAGE_ADD_DIR, fileName);
                       String contentsID = other.getBlobMap().get(fileName);
                       writeContents(tobeAdded, getContents(contentsID));
                       addStage(tobeAdded);

                   }
                   else if (isModified(fileName, headCommit.getBlobMap(), other.getBlobMap())) {
                       //TODO: modified in Other and HEAD in the same way --> DNM
                   }
                   else if (!isModified(fileName, headCommit.getBlobMap(), other.getBlobMap())) {
                       //TODO: modified in Other and HEAD in a different way --> Confilict

                   }
               }
               else {
                   if (!isModified(fileName, splitPoint.getBlobMap(), headCommit.getBlobMap())){
                       if (!otherFileSet.contains(fileName)){
                           //TODO: not modified in HEAD but not present in other --> Remove
                           File tobeRemoved = join(STAGE_RM_DIR, fileName);
                           String contentsID = other.getBlobMap().get(fileName);
                           writeContents(tobeRemoved, getContents(contentsID));
                           rmStage(tobeRemoved);
                       }
                   }
                   else if (!isModified(fileName, splitPoint.getBlobMap(), headCommit.getBlobMap())){
                       if (!headCommitFileSet.contains(fileName)){
                           //TODO: not modified in Other but not present in HEAD --> Remain Removed
                       }
                   }
               }
            }
        }
    }
}
