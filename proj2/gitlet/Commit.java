package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class

import java.util.HashMap;

import static gitlet.Utils.readContents;
import static gitlet.Utils.sha1;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /** The message of this Commit. */
    private String message;
    private Date timestamp;
    private HashMap<String, String> blobMap;
    private String parent;
    private String otherParent;
    private String ID;

    public Commit(String message, String parent, String parent2, Date time, HashMap<String,String> blobMap) {
        this.message = message;
        this.parent = parent;
        this.otherParent = parent2;
        if (this.parent == null) {
            this.timestamp = new Date(70, 0, 1, 0, 0, 0);
        } else {
            this.timestamp = time;
        }
        if (blobMap == null){
            this.blobMap = new HashMap(4,1);
        } else if (blobMap != null){
            this.blobMap = blobMap;
        }
    }

    public String getID(){
        return sha1(this);
    }

    public String getMessage(){
        return this.message;
    }

    public String getParent()  { return this.parent; }

    public String getOtherParent() { return this.otherParent; }

    public Date getTimestamp() { return this.timestamp; }

    public HashMap<String, String> getBlobMap(){ return this.blobMap; }

    public void setMessage(String msg){
        this.message = msg;
    }

    public void setParent(String sha1){
        this.parent = sha1;
    }

    public void setTimestamp(Date time){
        this.timestamp = time;
    }

    public void putBlobMap(String fileName, String contentsSha1){
        blobMap.put(fileName, contentsSha1);
    }

    public void removeFromBlobMap(String fileName){
        blobMap.remove(fileName);
    }
    /**
     * This method checks if the given file name is tracked in this commit.
     * It returns false if the given file name does not exist in the head commit.
     * Or, it returns false as well when the given file does exist but has a different contents from what HEAD commit is keeping track of.
     * (it checks
     * Otherwise, it returns true.
     */
    public Boolean isTracking(File f){
        if (this.blobMap.isEmpty() || !this.blobMap.containsKey(f.getName())){
            return false;
        }
        else if (!sha1(readContents(f)).equals(this.blobMap.get(f.getName()))){
            return false;
        }
        return true;
    }

    public Commit clone(){
        return new Commit(this.getMessage(), this.getParent(), this.getOtherParent(), this.getTimestamp(), this.getBlobMap());
    }
}

