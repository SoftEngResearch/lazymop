package edu.lazymop.tinymop.monitoring.datastructure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;

public class Trie {
    public static class Node {
        public int event;
        public int monitors = 0; // only used during trace collection
        public HashMap<Integer, Node> children = new HashMap<>();
        public IntIntHashMap childrenKeys = new IntIntHashMap();
        private final IntIntHashMap testCounts = new IntIntHashMap();

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

        public void incrementTestCount(int testId) {
            if (testId <= 0) {
                return;
            }
            testCounts.addToValue(testId, 1);
        }

        public void decrementTestCount(int testId) {
            if (testId <= 0) {
                return;
            }
            if (!testCounts.containsKey(testId)) {
                return;
            }
            int newValue = testCounts.get(testId) - 1;
            if (newValue <= 0) {
                testCounts.remove(testId);
            } else {
                testCounts.put(testId, newValue);
            }
        }

        public Map<Integer, Integer> snapshotTestCounts() {
            if (testCounts.isEmpty()) {
                return Collections.emptyMap();
            }

            Map<Integer, Integer> snapshot = new HashMap<>(testCounts.size());
            testCounts.forEachKeyValue(snapshot::put);
            return snapshot;
        }
    }

    public final Node root = new Node(0);
}
