// CHECKSTYLE:OFF
package edu.lazymop.deinstrumentation;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Stream;

public class Patcher {
    public static void patchJar(String jarPath) {
        URI jarFile = URI.create("jar:file:" + jarPath);
        try (FileSystem fs = FileSystems.newFileSystem(jarFile, new HashMap<String, String>())) {
            // Delete signature
            // zip -d ${JAR} "META-INF/*.SF" "META-INF/*.DSA" "META-INF/*.RSA" "META-INF/*.EC"
            try (Stream<Path> paths = Files.walk(fs.getPath("META-INF"), 1)) {
//                PathMatcher matcher = fs.getPathMatcher("glob:META-INF/*.{SF,DSA,RSA,EC}");
                paths
//                    .filter(matcher::matches)  // Only works on macOS
                        .forEach(p -> {
                            try {
                                String f = p.toString();
                                if (f.endsWith(".SF") || f.endsWith(".DSA") || f.endsWith(".RSA") || f.endsWith(".EC")) {
//                                    System.out.println("DELETE signature file " + f + " in " + jarPath);
                                    Files.delete(p);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
