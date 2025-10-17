package edu.lazymop.tinymop.monitoring.datastructure;

import java.util.List;

public class Pair {
    public Trie.Node node;
    public List<String> events;
    public boolean fromChangedClasses = false;

    public Pair(Trie.Node node, List<String> events) {
        this.node = node;
        this.events = events;
    }
}
