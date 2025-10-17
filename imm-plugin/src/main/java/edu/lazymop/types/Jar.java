package edu.lazymop.types;

import java.io.Serializable;

public class Jar implements Serializable {
    public String jarPath;
    public String groupId;
    public String artifactId;
    public String version;

    public Jar(String jarPath, String groupId, String artifactId, String version) {
        this.jarPath = jarPath;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Jar jar = (Jar) obj;
        return jarPath.equals(jar.jarPath) && groupId.equals(jar.groupId) && artifactId.equals(jar.artifactId)
                && version.equals(jar.version);
    }

    @Override
    public int hashCode() {
        return 31 * jarPath.hashCode() + groupId.hashCode() + artifactId.hashCode() + version.hashCode();
    }

    @Override
    public String toString() {
        return "Jar(" + this.jarPath + ", " + this.groupId + ", " + this.artifactId + ", " + this.version + ")";
    }

    public String getID() {
        return this.groupId + "-" + this.artifactId + "-" + this.version;
    }
}
