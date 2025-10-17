// CHECKSTYLE:OFF
package edu.lazymop.tinymop.monitoring.slicing.algod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import com.runtimeverification.rvmonitor.java.rt.ref.CachedWeakReference;
import com.runtimeverification.rvmonitor.java.rt.table.MapOfMonitor;
import edu.lazymop.tinymop.monitoring.MonitorManager;
import edu.lazymop.tinymop.monitoring.datastructure.Trie;
import edu.lazymop.tinymop.monitoring.util.SpecializedSlicingAlgorithmUtil;


public class IteratorHasNext {

    static final ReentrantLock lock = new ReentrantLock();

    private static boolean activated = false;

    // Cache for i indexing tree
    private static Object i_Map_cachekey_i;
    private static IteratorHasNextMonitor i_Map_cachevalue;

    private static final MapOfMonitor<IteratorHasNextMonitor> i_Map = new MapOfMonitor<>(0) ;
    private static final Trie trie = new Trie(); // store unique traces here

    public static final void hasnexttrue(Iterator i, boolean b, int event) {
        activated = true;
        while (!lock.tryLock()) {
            Thread.yield();
        }

//        System.out.println("hasnexttrue - (" + System.identityHashCode(i) + ", " + b + ") - event: " + event);

        CachedWeakReference wr_i = null;
        MapOfMonitor<IteratorHasNextMonitor> matchedLastMap = null;
        IteratorHasNextMonitor matchedEntry = null;
        boolean cachehit = false;
        if ((i == i_Map_cachekey_i) ) {
            matchedEntry = i_Map_cachevalue;
            cachehit = true;
        }
        else {
            wr_i = new CachedWeakReference(i) ;
            {
                // FindOrCreateEntry
                MapOfMonitor<IteratorHasNextMonitor> itmdMap = i_Map;
                matchedLastMap = itmdMap;
                IteratorHasNextMonitor node_i = i_Map.getNodeEquivalent(wr_i) ;
                matchedEntry = node_i;
            }
        }
        // D(X) main:1
        if ((matchedEntry == null) ) {
            if ((wr_i == null) ) {
                wr_i = new CachedWeakReference(i) ;
            }
            // D(X) main:4
            IteratorHasNextMonitor created = new IteratorHasNextMonitor(trie.root) ;
            matchedEntry = created;
            matchedLastMap.putNode(wr_i, created) ;
        }
        // D(X) main:8--9
        matchedEntry.hasnexttrue(i, b, event);

        if ((cachehit == false) ) {
            i_Map_cachekey_i = i;
            i_Map_cachevalue = matchedEntry;
        }

        lock.unlock();
    }

    public static final void hasnextfalse(Iterator i, boolean b, int event) {
        activated = true;
        while (!lock.tryLock()) {
            Thread.yield();
        }

//        System.out.println("hasnextfalse - (" + System.identityHashCode(i) + ", " + b + ") - event: " + event);

        CachedWeakReference wr_i = null;
        MapOfMonitor<IteratorHasNextMonitor> matchedLastMap = null;
        IteratorHasNextMonitor matchedEntry = null;
        boolean cachehit = false;
        if ((i == i_Map_cachekey_i) ) {
            matchedEntry = i_Map_cachevalue;
            cachehit = true;
        }
        else {
            wr_i = new CachedWeakReference(i) ;
            {
                // FindOrCreateEntry
                MapOfMonitor<IteratorHasNextMonitor> itmdMap = i_Map;
                matchedLastMap = itmdMap;
                IteratorHasNextMonitor node_i = i_Map.getNodeEquivalent(wr_i) ;
                matchedEntry = node_i;
            }
        }
        // D(X) main:1
        if ((matchedEntry == null) ) {
            if ((wr_i == null) ) {
                wr_i = new CachedWeakReference(i) ;
            }
            // D(X) main:4
            IteratorHasNextMonitor created = new IteratorHasNextMonitor(trie.root) ;
            matchedEntry = created;
            matchedLastMap.putNode(wr_i, created) ;
        }
        // D(X) main:8--9
        matchedEntry.hasnextfalse(i, b, event);

        if ((cachehit == false) ) {
            i_Map_cachekey_i = i;
            i_Map_cachevalue = matchedEntry;
        }

        lock.unlock();
    }

    public static final void next(Iterator i, int event) {
        activated = true;
        while (!lock.tryLock()) {
            Thread.yield();
        }

//        System.out.println("next - (" + System.identityHashCode(i) + ") - event: " + event);

        CachedWeakReference wr_i = null;
        MapOfMonitor<IteratorHasNextMonitor> matchedLastMap = null;
        IteratorHasNextMonitor matchedEntry = null;
        boolean cachehit = false;
        if ((i == i_Map_cachekey_i) ) {
            matchedEntry = i_Map_cachevalue;
            cachehit = true;
        }
        else {
            wr_i = new CachedWeakReference(i) ;
            {
                // FindOrCreateEntry
                MapOfMonitor<IteratorHasNextMonitor> itmdMap = i_Map;
                matchedLastMap = itmdMap;
                IteratorHasNextMonitor node_i = i_Map.getNodeEquivalent(wr_i) ;
                matchedEntry = node_i;
            }
        }
        // D(X) main:1
        if ((matchedEntry == null) ) {
            if ((wr_i == null) ) {
                wr_i = new CachedWeakReference(i) ;
            }
            // D(X) main:4
            IteratorHasNextMonitor created = new IteratorHasNextMonitor(trie.root) ;
            matchedEntry = created;
            matchedLastMap.putNode(wr_i, created) ;
        }
        // D(X) main:8--9
        matchedEntry.next(i, event);

        if ((cachehit == false) ) {
            i_Map_cachekey_i = i;
            i_Map_cachevalue = matchedEntry;
        }

        lock.unlock();
    }

    public static void monitorSlices(String specName, MonitorManager monitorManager) {
        SpecializedSlicingAlgorithmUtil.monitorSlices(specName, monitorManager, trie.root);
    }
}

class IteratorHasNextMonitor extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractSynchronizedMonitor implements Cloneable, com.runtimeverification.rvmonitor.java.rt.RVMObject {
    protected Object clone() {
        try {
            IteratorHasNextMonitor ret = (IteratorHasNextMonitor) super.clone();
            return ret;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    int state;
    Trie.Node node;

    IteratorHasNextMonitor(Trie.Node node) {
        this.node = node;
        this.node.monitors += 1;
        state = 0;
    }

    @Override
    public final int getState() {
        return state;
    }

    final void hasnexttrue(Iterator i, boolean b, int event) {
        if (b) {
            see(event);
        }
    }

    final void hasnextfalse(Iterator i, boolean b, int event) {
        if (!b) {
            see(event);
        }
    }

    final void next(Iterator i, int event) {
        see(event);
    }

    private void see(int eID) {
        node.monitors -= 1;
        node = node.getNextNodeAfterSeeingEvent(eID);
        node.monitors += 1;
    }

    @Override
    protected final void terminateInternal(int idnum) {}

}
