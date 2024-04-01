# Gitlet Design Document

**Name**: Seongmin Na

## Classes and Data Structures

### Commit
#### Instance Variables
* Message: contains
* Timestamp:
* Parent: 
* blobMap: is mapping fileName to SHA1 of the file's content.

    #### Methods
    * isTracking(File f) :
      this method checks whether or not the given file is being tracked by the commit. 
      it returns false if the file name is not the key in the blobMap or the content is different, meaning that 
      it needs to be staged for addition.
    * Field 2:


### Repository
#### Instance Variables
* Branch: Commit
* HEAD: Commit
* Master: Commit

    #### Fields
    * Initialize()
    * saveHEAD()


## Algorithms

## Persistence

### staging:
* TYPE: FILE as it is.
### blobs
* TYPE: SHA1 and the contents of the file.
### HEAD
* TYPE: Commit Object.

