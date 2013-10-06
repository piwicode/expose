package org.piwicode.expose;

/**
 * Accumulates statistics regarding the album file structure composition.
 * the statistics are reported by the working thread and displayed to
 * the user interface.
 */
public class Stat {

    protected int nbPhoto;
    protected int nbAlbum;

    protected int nbPhotoCreated;
    protected int nbPhotoDeleted;
    protected int nbAlbumCreated;
    protected int nbAlbumDeleted;
    protected int nbStaredPhoto;
    protected int nbStaredAlbum;

    protected int nbAlbumToProcess;
    protected int nbAlbumDone;
    protected int nbFile;

    public void incFile() {
        nbFile++;
    }


    public int nbFile() {
        return nbFile;
    }

    public int incAlbum() {
        return ++nbAlbum;

    }

    public int nbAlbum() {
        return nbAlbum;
    }

    void warn(String str, Object... args) {
        System.err.printf(str, args);
        System.err.println();
    }

    public void info(String str, Object... args) {
        System.out.printf(str, args);
        System.out.println();
    }

    public void error(String str, Object... args) {
        System.err.printf(str, args);
        System.err.println();
    }

    public int incPhoto() {
        return ++nbPhoto;
    }

    public int incStaredPhoto() {
        return ++nbStaredPhoto;
    }

    public int incStaredAlbum() {
        return ++nbStaredAlbum;
    }

    public int incPhotoDeleted() {
        return ++nbPhotoDeleted;
    }

    public int incPhotoCreated() {
        return ++nbPhotoCreated;
    }

    public int incAlbumDeleted() {
        return ++nbAlbumDeleted;
    }
    public int incAlbumToProcess() {
        return ++nbAlbumToProcess;
    }

    public int incAlbumDone() {
        return ++nbAlbumDone;
    }

    public int incAlbumCreated() {
        return ++nbAlbumCreated;
    }
}
