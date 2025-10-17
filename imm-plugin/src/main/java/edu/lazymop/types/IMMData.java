package edu.lazymop.types;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.lazymop.FileUtil;

public class IMMData {
    public static Map<String, Map<String, Boolean>> methodsIsolationStatus = null;
    public static Map<String, Map<String, Map<String, Integer>>> statistics = null;
    public static Map<String, Integer> sortedHotMethods = null;
    public static Map<String, String> classToJar = null;
    public static Set<String> changedClasses = new HashSet<>();
    public static Set<String> containLoopClasses = new HashSet<>();
    public static Set<String> deIMMClasses = new HashSet<>();

    public static Set<String> transformedClasses = new HashSet<>();
    public static Set<String> transformedTestClasses = new HashSet<>();
    public static Set<String> transformedLibraryClasses = new HashSet<>();

    public static long timeToRunLocator = 0;
    public static long timeToRunRemover = 0;
    public static long testStartTime = 0;

    public static boolean writeStatisticsToFile() {
        try {
            Path savedFile = Paths.get(FileUtil.getArtifactDir() + File.separator + "statistics.imm");
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(savedFile))) {
                oos.writeObject(statistics);
            }
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    public static boolean writeClassToJarToFile() {
        try {
            Path savedFile = Paths.get(FileUtil.getArtifactDir() + File.separator + "classToJar.imm");
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(savedFile))) {
                oos.writeObject(classToJar);
            }

            savedFile = Paths.get(FileUtil.getArtifactDir() + File.separator + "containForLoop.imm");
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(savedFile))) {
                oos.writeObject(containLoopClasses);
            }

            savedFile = Paths.get(FileUtil.getArtifactDir() + File.separator + "deIMMClasses.imm");
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(savedFile))) {
                oos.writeObject(deIMMClasses);
            }
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    public static Set<String> getPreviousClasspathAndUpdate(Set<String> newClasspath) {
        try {
            Path savedFile = Paths.get(FileUtil.getArtifactDir() + File.separator + "classpath.imm");
            Set<String> oldClasspath = null;
            if (Files.exists(savedFile)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(savedFile))) {
                    oldClasspath = (HashSet<String>) ois.readObject();
                }
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(savedFile))) {
                oos.writeObject(newClasspath);
            }
            return oldClasspath;
        } catch (IOException | ClassNotFoundException exception) {
            return null;
        }
    }

    public static boolean readStatisticsFromFile() {
        try {
            Path savedFile = Paths.get(FileUtil.getArtifactDir() + File.separator + "statistics.imm");
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(savedFile))) {
                statistics = (Map<String, Map<String, Map<String, Integer>>>) ois.readObject();
            }
            return true;
        } catch (IOException | ClassNotFoundException ioe) {
            return false;
        }
    }

    public static void readClassToJarToFile() {
        try {
            Path savedFile = Paths.get(FileUtil.getArtifactDir() + File.separator + "classToJar.imm");
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(savedFile))) {
                classToJar = (Map<String, String>) ois.readObject();
            }

            savedFile = Paths.get(FileUtil.getArtifactDir() + File.separator + "containForLoop.imm");
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(savedFile))) {
                containLoopClasses = (Set<String>) ois.readObject();
            }

            savedFile = Paths.get(FileUtil.getArtifactDir() + File.separator + "deIMMClasses.imm");
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(savedFile))) {
                deIMMClasses = (Set<String>) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException ioe) {
            classToJar = new HashMap<>();
        }
    }
}
