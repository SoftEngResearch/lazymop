package edu.lazymop.tinymop.monitoring;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.lazymop.tinymop.monitoring.datastructure.Trie;
import edu.lazymop.tinymop.monitoring.monitorsmanager.IteratorHasNextMonitorManager;
import edu.lazymop.tinymop.monitoring.util.SpecializedSlicingAlgorithmUtil;
import org.junit.After;
import org.junit.Test;


public class SpecializedSlicingAlgorithmUtilTest {

    @After
    public void cleanUp() throws IOException {
        Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + File.separator + "testSpec-violations"));
        Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + File.separator + "testSpec-traces"));
        Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + File.separator + "testSpec-changedTraces"));
        Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + File.separator + "testSpec-error"));
        Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + File.separator + "testSpec-locations"));
    }

    private void addTrace(Trie trie, MonitorManager manager, int[] eventID, int[] eventLOC) {
        assertEquals(eventID.length, eventLOC.length);
        Trie.Node node = trie.root;
        node.monitors += 1;

        for (int i = 0; i < eventID.length; i++) {
            manager.notifyMapping(eventLOC[i], "Dummy.java:" + eventLOC[i], false);

            node.monitors -= 1;
            node = node.getNextNodeAfterSeeingEvent((eventLOC[i] << 4) | eventID[i]);
            node.monitors += 1;
        }
    }

    private void compareTraces(List<String> expectedTraces, int binding)  {
        try {
            List<String> traces = new ArrayList<>();
            for (String line : Files.readAllLines(Paths.get(System.getProperty("user.dir")
                    + File.separator + "testSpec-traces"))) {
                if (!line.startsWith("Total")) {
                    traces.add(line);
                } else {
                    assertEquals(line, "Total " + binding + " bindings");
                }
            }

            Collections.sort(traces);
            Collections.sort(expectedTraces);
            assertArrayEquals(expectedTraces.toArray(), traces.toArray());
        } catch (IOException ioe) {
            fail();
        }
    }

    private void compareViolations(List<String> expectedViolations)  {
        try {
            List<String> violations = new ArrayList<>();
            for (String line : Files.readAllLines(Paths.get(System.getProperty("user.dir")
                    + File.separator + "testSpec-violations"))) {
                if (!line.startsWith("COLLECT") && !line.startsWith("MONITOR")) {
                    violations.add(line);
                }
            }

            Collections.sort(violations);
            Collections.sort(expectedViolations);
            assertArrayEquals(expectedViolations.toArray(), violations.toArray());
        } catch (IOException ioe) {
            fail();
        }
    }

    @Test
    public void testIteratorHasNext() {
        IteratorHasNextMonitorManager manager = new IteratorHasNextMonitorManager();

        Trie trie = new Trie();
        addTrace(trie, manager, new int[]{3}, new int[]{1});
        addTrace(trie, manager, new int[]{3}, new int[]{1});
        addTrace(trie, manager, new int[]{3, 3, 3}, new int[]{1, 2, 3});
        addTrace(trie, manager, new int[]{3, 3, 3}, new int[]{1, 2, 10});
        addTrace(trie, manager, new int[]{1, 2, 3}, new int[]{7, 8, 9});
        addTrace(trie, manager, new int[]{1, 2, 3}, new int[]{7, 8, 9});
        addTrace(trie, manager, new int[]{1, 3, 2}, new int[]{7, 8, 9});
        addTrace(trie, manager, new int[]{1, 3, 2}, new int[]{7, 8, 9});

        SpecializedSlicingAlgorithmUtil.monitorSlices("testSpec", manager, trie.root);
        compareTraces(Arrays.asList(
                "2 [E3~1]",
                "1 [E3~1, E3~2, E3~3]",
                "1 [E3~1, E3~2, E3~10]",
                "2 [E1~7, E2~8, E3~9]",
                "2 [E1~7, E3~8, E2~9]"
        ), 8);

        compareViolations(Arrays.asList(
                "2 [E3~1]",
                "1 [E3~1, E3~2, E3~3]",
                "1 [E3~1, E3~2, E3~10]",
                "2 [E1~7, E2~8, E3~9]"
        ));
    }
}
