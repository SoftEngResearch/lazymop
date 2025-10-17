package edu.lazymop.types;

public class Task {
    public enum Type {
        JAR,
        PROJECT
    }

    private final Type type;
    private Jar jar = null;
    private final String src;
    private String dest = null;

    public Task(Type type, String src) {
        this.type = type;
        this.src = src;
    }

    public Task(Jar jar, String src) {
        this.type = Type.JAR;
        this.jar = jar;
        this.src = src;
    }

    public Task(Type type, String src, String dest) {
        this(type, src);
        this.dest = dest;
    }

    public boolean isJar() {
        return this.type == Type.JAR;
    }

    public String getSource() {
        return this.src;
    }

    public String getDest() {
        return this.dest;
    }

    public Jar getJar() {
        return this.jar;
    }
}
