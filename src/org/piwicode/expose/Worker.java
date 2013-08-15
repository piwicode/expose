package org.piwicode.expose;

import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class Worker {
    private final Stat stat;

    public Worker(Stat stat) {
        this.stat = stat;
    }

    public void expose(final Path in, final Path out) throws IOException {
        if (Files.isDirectory(in) == false) {
            throw new IllegalArgumentException("Can't find Picasa library at " + in);
        }
        if (Files.isDirectory(out.getParent()) == false) {
            throw new IllegalArgumentException("Can't find output out at " + out);
        }

        Files.createDirectories(out);

        final List<Path> picasaDataPaths = findPicasaFiles(in);
        final Set<String> albums = new HashSet<>();
        for (Path picasaDataPath : picasaDataPaths) {
            try {
                processPicasaAlbum(in, out, albums, picasaDataPath);
            } catch (IOException e) {
                stat.error("Unable to process album %s: %s", picasaDataPath.getParent(), e.getMessage());
            } finally {
                stat.incAlbumDone();
            }
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(out)) {
            for (Path asset : directoryStream) {
                if (albums.remove(asset.getFileName().toString()) == false) {
                    stat.info("Delete %s", asset);
                    try {
                        stat.incAlbumDeleted();
                        deleteRecursively(asset);
                    } catch (IOException e) {
                        stat.error("Unable to remove album %s: %s", asset, e.getMessage());
                    }
                }
            }
        }
    }

    private void processPicasaAlbum(Path in, Path out, Set<String> albums, Path picasaDataPath) throws IOException {
        final Set<String> staredFiles;
        try {
            staredFiles = getStaredFiles(picasaDataPath);
        } catch (IOException e) {
            stat.warn("Ignore unparsable picasa data file %d", picasaDataPath);
            return;
        }
        if (staredFiles.isEmpty()) return;
        stat.incStaredAlbum();

        final String albumName = composeAlbumName(in.relativize(picasaDataPath).getParent().toString());
        albums.add(albumName);
        final Path inAlbumPath = picasaDataPath.getParent(), outAlbumPath = out.resolve(albumName);
        try {
            Files.createDirectory(outAlbumPath);
            stat.incAlbumCreated();
        } catch (FileAlreadyExistsException e) {
        }
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(outAlbumPath)) {
            for (Path asset : directoryStream) {
                if (staredFiles.remove(asset.getFileName().toString()) == false) {
                    stat.info("Delete " + asset);
                    try {
                        stat.incPhotoDeleted();
                        deleteRecursively(asset);
                    } catch (IOException e) {
                        stat.warn("Unable to remove photo %s", asset);
                    }
                }
            }
        }

        for (String staredFileName : staredFiles) {
            final Path inFile = inAlbumPath.resolve(staredFileName);
            final Path outFile = outAlbumPath.resolve(staredFileName);
            stat.info("create " + outFile);
            try {
                stat.incPhotoCreated();
                Files.createLink(outFile, inFile);
            } catch (NoSuchFileException ex) {
                stat.warn("%s is stared but does not exist", inFile);
            }
        }
        stat.info("Album " + in.relativize(picasaDataPath) + " has " + staredFiles.size() + " star(s)");
    }

    private void deleteRecursively(Path fileOrDirectory) throws IOException {
        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(fileOrDirectory, visitor);
    }

    List<Path> findPicasaFiles(final Path source) throws IOException {

        final List<Path> picasaPaths = new ArrayList<>();
        final FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            final PathMatcher photo = source.getFileSystem().getPathMatcher("glob:*.{jpg,mov,avi,mp4}");
            final PathMatcher picasa = source.getFileSystem().getPathMatcher("glob:{.picasa.ini,Picasa.ini}");
            final PathMatcher ignore = source.getFileSystem().getPathMatcher("glob:{Thumbs.db,ZbThumbnail.info,*.doc}");
            final Set<Path> alb = new HashSet<Path>();

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                final Path fileName = file.getFileName();
                if (picasa.matches(fileName)) {
                    picasaPaths.add(file);
                    stat.incAlbumToProcess();
                } else if (photo.matches(fileName)) {
                    if (alb.add(file.getParent()) == true)
                        stat.incAlbum();
                    stat.incPhoto();
                } else if (ignore.matches(fileName)) {
                } else {
                    stat.warn("Unknown object found it the input %s", fileName);
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(source, visitor);
        stat.info("Picasa found: " + picasaPaths.size());
        stat.info("Album found: " + stat.nbAlbum());
        stat.info("Directory: " + stat.nbAlbum());
        stat.info("File: " + stat.nbFile());
        return picasaPaths;
    }

    private Set<String> getStaredFiles(Path picasaPath) throws IOException {
        final Ini ini;
        try (InputStream is = Files.newInputStream(picasaPath)) {
            ini = new Ini(is);
        }
        final Set<String> staredFiles = new HashSet<>();
        for (Map.Entry<String, Profile.Section> e : ini.entrySet()) {
            final Profile.Section section = e.getValue();
            final String starStr = section.get("star");
            if (starStr == null) {
            } else if (starStr.equalsIgnoreCase("yes")) {
                staredFiles.add(e.getKey());
                stat.incStaredPhoto();
            } else {
                stat.info(picasaPath + " - " + e.getKey() + " - " + starStr);
            }
        }

        return staredFiles;
    }

    private String composeAlbumName(String s) {
        return s.replace("\\", "-");
    }


}

