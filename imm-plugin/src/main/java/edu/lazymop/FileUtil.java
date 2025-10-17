package edu.lazymop;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.ekstazi.hash.Hasher;

public class FileUtil {

    private static String artifactDirectory;

    public static void setArtifactDir(String projectDir) {
        if (artifactDirectory == null) {
            artifactDirectory = projectDir + File.separator + ".tinymop";

            createDirectories(Paths.get(getArtifactDir()));
            createDirectories(Paths.get(getOriginalBytecodeDir()));
            createDirectories(Paths.get(getTransformedBytecodeDir()));
            createDirectories(Paths.get(getOriginalBytecodeProject()));
            createDirectories(Paths.get(getOriginalBytecodeLibrary()));
            createDirectories(Paths.get(getTransformedBytecodeProject()));
            createDirectories(Paths.get(getTransformedBytecodeLibrary()));
        }
    }

    public static String getArtifactDir() {
        return artifactDirectory;
    }

    public static String getOriginalBytecodeDir() {
        return getArtifactDir() + File.separator + "original";
    }

    public static String getTransformedBytecodeDir() {
        return getArtifactDir() + File.separator + "transformed";
    }

    public static String getOriginalBytecodeProject() {
        return getOriginalBytecodeDir() + File.separator + "project";
    }

    public static String getOriginalBytecodeLibrary() {
        return getOriginalBytecodeDir() + File.separator + "library";
    }

    public static String getTransformedBytecodeProject() {
        return getTransformedBytecodeDir() + File.separator + "project";
    }

    public static String getTransformedBytecodeLibrary() {
        return getTransformedBytecodeDir() + File.separator + "library";
    }

    public static Set<String> getChangedClasses(Path pathInTarget, Path pathInArtifact, boolean cleanBytecode) {
        Hasher hasher = new Hasher(Hasher.Algorithm.CRC32, 1000, true);
        Set<String> changedFiles = new HashSet<>();
        try {
            Files.walkFileTree(pathInTarget, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relativePath = pathInTarget.relativize(file);
                    Path pathInDest = pathInArtifact.resolve(relativePath);
                    String fileName = relativePath.toString();
                    if (fileName.endsWith(".class")) {
                        if (!Files.exists(pathInDest)) {
                            // .class file in target but not in previous classes
                            changedFiles.add(fileName);
                        } else {
                            if (!FileUtils.contentEquals(file.toFile(), pathInDest.toFile())) {
                                if (cleanBytecode) {
                                    String newFileHash = hasher.hashURL("file:" + file);
                                    String oldFileHash = hasher.hashURL("file:" + pathInDest);
                                    if (!newFileHash.equals(oldFileHash)) {
                                        changedFiles.add(fileName);
                                    }
                                } else {
                                    changedFiles.add(fileName);
                                }
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return changedFiles;
    }

    public static void createDirectories(Path dir) {
        if (Files.exists(dir)) {
            return;
        }

        try {
            Files.createDirectories(dir);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void copy(Path src, Path dest, boolean createIfNotExists) throws IOException {
        if (createIfNotExists) {
            createDirectories(dest.getParent());
        }

        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void copyToJar(String jarPath, Path src, String dest) throws IOException {
        try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:" + jarPath),
                new HashMap<String, String>())) {
            Path destPath = fs.getPath(dest);
            Files.copy(src, destPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static Set<String> listFilesInJar(String jar) {
        Set<String> extracted = new HashSet<>();

        try {
            URI jarFile = URI.create("jar:file:" + jar);
            try (FileSystem fs = FileSystems.newFileSystem(jarFile, new HashMap<String, String>())) {
                Path jarRoot = fs.getPath("/");
                Files.walkFileTree(jarRoot, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String relPath = jarRoot.relativize(file).toString();
//                        Path pathInTmp = dest.resolve(relPath);
//                        if (wantedClasses.contains(relPath)) {
//                            Files.copy(file, pathInTmp, StandardCopyOption.REPLACE_EXISTING);
//                        }
                        if (relPath.endsWith(".class")) {
                            extracted.add(relPath);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                        Path pathInTmp = dest.resolve(jarRoot.relativize(dir).toString());
//                        Files.createDirectories(pathInTmp);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return extracted;
    }

    public static void copyDirectory(Path src, Path dest) {
        try {
            Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = dest.resolve(src.relativize(file));
                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = dest.resolve(src.relativize(dir));
                    Files.createDirectories(targetPath);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
