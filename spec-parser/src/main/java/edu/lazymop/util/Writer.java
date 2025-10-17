package edu.lazymop.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

public class Writer {
    private static final Logger LOGGER = Logger.getGlobal();

    public static void write(String contents, File directory, String fileName) {
        try (BufferedWriter writer = getWriter(new File(directory, fileName.replace(".mop", ".aj")).getAbsolutePath())) {
            writer.write(contents + System.lineSeparator());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static BufferedWriter getWriter(String filePath) {
        Path path = Paths.get(filePath);
        BufferedWriter writer = null;
        try {
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return writer;
    }

    public static void persistEnableSets(String fileName, String enableSets) {
        Path file = Paths.get("src/main/resources/enableSets/", fileName);
        try (BufferedWriter writer = getWriter(file.toString())) {
            writer.write(enableSets + System.lineSeparator());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void persistLessInformative(String fileName,
                                              Map<Set<String>, TreeSet<Map<String, Integer>>> lessInfo) {
        Path file = Paths.get("src/main/resources/lessInfo/", fileName);
        try (BufferedWriter writer = getWriter(file.toString())) {
            for (Map.Entry<Set<String>, TreeSet<Map<String, Integer>>> lessInfoEntry : lessInfo.entrySet()) {
                writer.write(lessInfoEntry.getKey() + "::" + lessInfoEntry.getValue() + System.lineSeparator());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
