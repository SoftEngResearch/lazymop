package edu.lazymop.tinymop.specparser;

import java.io.File;
import java.net.URL;

public class TestUtil {
    public static File getResourceFile(String specName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(specName);
        if (url == null) {
            return null;
        }

        return new File(url.getPath());
    }
}
