package edu.lazymop.tinymop.monitoring.datastructure;

import java.util.HashMap;

import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;

public class Trie {
    public static class Node {
        public int event;
        public int monitors = 0; // only used during trace collection
        public HashMap<Integer, Node> children = new HashMap<>();
        public IntIntHashMap childrenKeys = new IntIntHashMap();

        public Node(int event) {
            this.event = event;
        }

        /**
         * Retrieves the next node after seeing the given event. If the event does not exist,
         * a new node is created and added to the children.
         *
         * @param event the event to look for
         * @return the next node associated with the event
         */
        public Node getNextNodeAfterSeeingEvent(Integer event) {
            if (childrenKeys.containsKey(event)) {
                return children.get(event);
            }

            Node nextEvent = new Node(event);
            children.put(event, nextEvent);
            childrenKeys.put(event, 0);
            return nextEvent;
        }
    }

    public final Node root = new Node(0);
}
