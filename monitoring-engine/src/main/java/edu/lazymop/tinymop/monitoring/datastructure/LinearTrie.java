package edu.lazymop.tinymop.monitoring.datastructure;

import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;

import java.util.*;

public class LinearTrie extends Trie {

    public static class RawEventNode {
        public int event;
        public int frequency = 1;

        public RawEventNode(int event) {
            this.event = event;
        }
    }


    public static class LinearNode extends Trie.Node {
        public RawEventNode lastEvent = null;

        public int monitors = 0; // only used during trace collection
        public List<RawEventNode> events = new ArrayList<>();
        public IntIntHashMap violatingEvents = new IntIntHashMap();

        private final Set<Integer> testCounts = new HashSet<>();

        public LinearNode(int event) {
            super(event);
        }

        /**
         * Retrieves the next node after seeing the given event. If the event does not exist,
         * a new node is created and added to the children.
         *
         * @param event the event to look for
         * @return the next node associated with the event
         */
        public void seeingEvent(Integer event) {
            if (lastEvent != null && lastEvent.event == event) {
                lastEvent.frequency += 1;
            } else {
                lastEvent = new RawEventNode(event);
                events.add(lastEvent);
            }
        }

        public void seeingViolatingEvent(Integer event) {
            violatingEvents.addToValue(event, 1);
        }

        public void incrementTestCount(int testId) {
            testCounts.add(testId);
        }

        public void decrementTestCount(int testId) {}

        public Map<Integer, Integer> snapshotTestCounts() {
            Map<Integer, Integer> map = new HashMap<>();
            for (Integer testId : testCounts) {
                map.put(testId, 1);
            }
            return map;
        }
    }

    public final LinearNode root = new LinearNode(0);
}
