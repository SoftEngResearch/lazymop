package edu.lazymop.tinymop.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;

import edu.lazymop.tinymop.monitoring.datastructure.Trie;
import org.junit.Test;

public class TrieTest {

    @Test
    public void testTrieAddFromRoot() {
        Trie trie = new Trie();
        Trie.Node node = trie.root.getNextNodeAfterSeeingEvent(1);
        assertEquals(1, node.event);
    }

    @Test
    public void testTrieAddFromChildren() {
        Trie trie = new Trie();
        Trie.Node node = trie.root.getNextNodeAfterSeeingEvent(1);
        Trie.Node node2 = node.getNextNodeAfterSeeingEvent(2);

        assertEquals(1, node.event);
        assertEquals(2, node2.event);
    }

    @Test
    public void testTrieAddMultipleFromRoot() {
        ArrayList<Trie.Node> nodes = new ArrayList<>();

        Trie trie = new Trie();
        for (int i = 1; i <= 100; i++) {
            nodes.add(trie.root.getNextNodeAfterSeeingEvent(i));
        }

        assertEquals(100, trie.root.children.size());
        for (int i = 1; i <= 100; i++) {
            assertEquals(nodes.get(i - 1), trie.root.getNextNodeAfterSeeingEvent(i));
        }
    }

    @Test
    public void testTrieAddMultipleFromChildren() {
        ArrayList<Trie.Node> nodes = new ArrayList<>();

        Trie trie = new Trie();
        Trie.Node node = trie.root.getNextNodeAfterSeeingEvent(1);

        for (int i = 1; i <= 100; i++) {
            nodes.add(node.getNextNodeAfterSeeingEvent(i));
        }

        assertEquals(1, trie.root.children.size());
        assertEquals(100, node.children.size());
        assertEquals(100, trie.root.getNextNodeAfterSeeingEvent(1).children.size());
        for (int i = 1; i <= 100; i++) {
            assertEquals(nodes.get(i - 1), node.getNextNodeAfterSeeingEvent(i));
            assertEquals(nodes.get(i - 1), trie.root.getNextNodeAfterSeeingEvent(1).getNextNodeAfterSeeingEvent(i));
        }
    }

    @Test
    public void testTrieAddDuplicatedFromRoot() {
        Trie trie = new Trie();
        Trie.Node node = trie.root.getNextNodeAfterSeeingEvent(123);

        assertEquals(node, trie.root.getNextNodeAfterSeeingEvent(123));

        Trie.Node node2 = node.getNextNodeAfterSeeingEvent(456);
        assertEquals(node, trie.root.getNextNodeAfterSeeingEvent(123));
        assertEquals(node2, node.getNextNodeAfterSeeingEvent(456));
        assertNotEquals(node, node.getNextNodeAfterSeeingEvent(456));
        assertNotEquals(node2, node2.getNextNodeAfterSeeingEvent(456));
    }
}
