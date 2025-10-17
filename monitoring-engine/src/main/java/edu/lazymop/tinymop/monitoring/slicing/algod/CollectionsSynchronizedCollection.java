// CHECKSTYLE:OFF
package edu.lazymop.tinymop.monitoring.slicing.algod;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import com.runtimeverification.rvmonitor.java.rt.ref.CachedWeakReference;
import com.runtimeverification.rvmonitor.java.rt.table.MapOfAll;
import com.runtimeverification.rvmonitor.java.rt.table.MapOfMonitor;
import com.runtimeverification.rvmonitor.java.rt.table.MapOfSetMonitor;
import com.runtimeverification.rvmonitor.java.rt.tablebase.DisableHolder;
import com.runtimeverification.rvmonitor.java.rt.tablebase.IDisableHolder;
import com.runtimeverification.rvmonitor.java.rt.tablebase.IMonitor;
import com.runtimeverification.rvmonitor.java.rt.tablebase.TableAdopter.Tuple2;
import com.runtimeverification.rvmonitor.java.rt.tablebase.TableAdopter.Tuple3;
import edu.lazymop.tinymop.monitoring.MonitorManager;
import edu.lazymop.tinymop.monitoring.datastructure.Trie;
import edu.lazymop.tinymop.monitoring.util.SpecializedSlicingAlgorithmUtil;

public class CollectionsSynchronizedCollection {

    static final ReentrantLock lock = new ReentrantLock();
    private static long CollectionsSynchronizedCollection_timestamp = 1;

    private static boolean activated = false;

    // Cache for col indexing tree
    private static Object col_Map_cachekey_col;
    private static Tuple3<MapOfMonitor<ICollectionsSynchronizedCollectionMonitor>, CollectionsSynchronizedCollectionMonitor_Set, CollectionsSynchronizedCollectionMonitor> col_Map_cachevalue;

    // Cache for iter indexing tree
    private static Object iter_Map_cachekey_iter;
    private static Tuple2<CollectionsSynchronizedCollectionMonitor_Set, ICollectionsSynchronizedCollectionMonitor> iter_Map_cachevalue;

    // Cache for (col, iter) indexing tree
    private static Object col_iter_Map_cachekey_col;
    private static Object col_iter_Map_cachekey_iter;
    private static ICollectionsSynchronizedCollectionMonitor col_iter_Map_cachevalue;

    private static final MapOfAll<MapOfMonitor<ICollectionsSynchronizedCollectionMonitor>, CollectionsSynchronizedCollectionMonitor_Set, CollectionsSynchronizedCollectionMonitor> CollectionsSynchronizedCollection_col_iter_Map = new MapOfAll<>(0);
    private static final MapOfSetMonitor<CollectionsSynchronizedCollectionMonitor_Set, ICollectionsSynchronizedCollectionMonitor> CollectionsSynchronizedCollection_iter_Map = new MapOfSetMonitor<>(1);

    private static final CollectionsSynchronizedCollectionMonitor_Set allSet = new CollectionsSynchronizedCollectionMonitor_Set();
    private static final Trie trie = new Trie(); // store unique traces here

    public static void sync(Collection col, int event) {
        activated = true;
        while (!lock.tryLock()) {
            Thread.yield();
        }

//        System.out.println("sync - (" + System.identityHashCode(col) + ") - event: " + event);

        CachedWeakReference wr_col = null;
        Tuple3<MapOfMonitor<ICollectionsSynchronizedCollectionMonitor>, CollectionsSynchronizedCollectionMonitor_Set, CollectionsSynchronizedCollectionMonitor> matchedEntry = null;
        boolean cachehit = false;
        if ((col == col_Map_cachekey_col) ) {
            matchedEntry = col_Map_cachevalue;
            cachehit = true;
        }
        else {
            wr_col = new CachedWeakReference(col);
            {
                // FindOrCreateEntry
                Tuple3<MapOfMonitor<ICollectionsSynchronizedCollectionMonitor>, CollectionsSynchronizedCollectionMonitor_Set, CollectionsSynchronizedCollectionMonitor> node_col = CollectionsSynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col);
                if ((node_col == null) ) {
                    node_col = new Tuple3<>();
                    CollectionsSynchronizedCollection_col_iter_Map.putNode(wr_col, node_col);
                    node_col.setValue1(new MapOfMonitor<>(1) );
                    node_col.setValue2(new CollectionsSynchronizedCollectionMonitor_Set() );
                }
                matchedEntry = node_col;
            }
        }
        // D(X) main:1
        CollectionsSynchronizedCollectionMonitor matchedLeaf = matchedEntry.getValue3();
        if ((matchedLeaf == null) ) {
            if ((wr_col == null) ) {
                wr_col = new CachedWeakReference(col);
            }
            if ((matchedLeaf == null) ) {
                // D(X) main:4
                CollectionsSynchronizedCollectionMonitor created = new CollectionsSynchronizedCollectionMonitor(CollectionsSynchronizedCollection_timestamp++, trie.root);
                matchedEntry.setValue3(created);
                CollectionsSynchronizedCollectionMonitor_Set enclosingSet = matchedEntry.getValue2();
                enclosingSet.add(created);
                allSet.add(created); // create new monitor
            }
            // D(X) main:6
            CollectionsSynchronizedCollectionMonitor disableUpdatedLeaf = matchedEntry.getValue3();
            disableUpdatedLeaf.setDisable(CollectionsSynchronizedCollection_timestamp++);
        }
        // D(X) main:8--9
        CollectionsSynchronizedCollectionMonitor_Set stateTransitionedSet = matchedEntry.getValue2();
        stateTransitionedSet.sync(col, event);

        if (!cachehit ) {
            col_Map_cachekey_col = col;
            col_Map_cachevalue = matchedEntry;
        }

        lock.unlock();
    }

    public static void syncCreateIter(Collection col, Iterator iter, int event) {
        while (!lock.tryLock()) {
            Thread.yield();
        }

//        System.out.println("syncCreateIter - (" + System.identityHashCode(col) + ", " + System.identityHashCode(iter) + ") - event: " + event);

        if (activated) {
            CachedWeakReference wr_col = null;
            CachedWeakReference wr_iter = null;
            MapOfMonitor<ICollectionsSynchronizedCollectionMonitor> matchedLastMap = null;
            ICollectionsSynchronizedCollectionMonitor matchedEntry = null;
            boolean cachehit = false;
            if (((col == col_iter_Map_cachekey_col) && (iter == col_iter_Map_cachekey_iter) ) ) {
                matchedEntry = col_iter_Map_cachevalue;
                cachehit = true;
            }
            else {
                wr_col = new CachedWeakReference(col);
                wr_iter = new CachedWeakReference(iter);
                {
                    // FindOrCreateEntry
                    Tuple3<MapOfMonitor<ICollectionsSynchronizedCollectionMonitor>, CollectionsSynchronizedCollectionMonitor_Set, CollectionsSynchronizedCollectionMonitor> node_col = CollectionsSynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col);
                    if ((node_col == null) ) {
                        node_col = new Tuple3<>();
                        CollectionsSynchronizedCollection_col_iter_Map.putNode(wr_col, node_col);
                        node_col.setValue1(new MapOfMonitor<>(1) );
                        node_col.setValue2(new CollectionsSynchronizedCollectionMonitor_Set() );
                    }
                    MapOfMonitor<ICollectionsSynchronizedCollectionMonitor> itmdMap = node_col.getValue1();
                    matchedLastMap = itmdMap;
                    ICollectionsSynchronizedCollectionMonitor node_col_iter = node_col.getValue1() .getNodeEquivalent(wr_iter);
                    matchedEntry = node_col_iter;
                }
            }
            // D(X) main:1
            if ((matchedEntry == null) ) {
                if ((wr_col == null) ) {
                    wr_col = new CachedWeakReference(col);
                }
                if ((wr_iter == null) ) {
                    wr_iter = new CachedWeakReference(iter);
                }
                {
                    // D(X) createNewMonitorStates:4 when Dom(theta'') = <col>
                    CollectionsSynchronizedCollectionMonitor sourceLeaf = null;
                    {
                        // FindCode
                        Tuple3<MapOfMonitor<ICollectionsSynchronizedCollectionMonitor>, CollectionsSynchronizedCollectionMonitor_Set, CollectionsSynchronizedCollectionMonitor> node_col = CollectionsSynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col);
                        if ((node_col != null) ) {
                            CollectionsSynchronizedCollectionMonitor itmdLeaf = node_col.getValue3();
                            sourceLeaf = itmdLeaf;
                        }
                    }
                    if ((sourceLeaf != null) ) {
                        boolean definable = true;
                        // D(X) defineTo:1--5 for <col, iter>
                        if (definable) {
                            // FindCode
                            Tuple3<MapOfMonitor<ICollectionsSynchronizedCollectionMonitor>, CollectionsSynchronizedCollectionMonitor_Set, CollectionsSynchronizedCollectionMonitor> node_col = CollectionsSynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col);
                            if ((node_col != null) ) {
                                ICollectionsSynchronizedCollectionMonitor node_col_iter = node_col.getValue1() .getNodeEquivalent(wr_iter);
                                if ((node_col_iter != null) ) {
                                    if (((node_col_iter.getDisable() > sourceLeaf.getTau() ) || ((node_col_iter.getTau() > 0) && (node_col_iter.getTau() < sourceLeaf.getTau() ) ) ) ) {
                                        definable = false;
                                    }
                                }
                            }
                        }
                        // D(X) defineTo:1--5 for <iter>
                        if (definable) {
                            // FindCode
                            Tuple2<CollectionsSynchronizedCollectionMonitor_Set, ICollectionsSynchronizedCollectionMonitor> node_iter = CollectionsSynchronizedCollection_iter_Map.getNodeEquivalent(wr_iter);
                            if ((node_iter != null) ) {
                                ICollectionsSynchronizedCollectionMonitor itmdLeaf = node_iter.getValue2();
                                if ((itmdLeaf != null) ) {
                                    if (((itmdLeaf.getDisable() > sourceLeaf.getTau() ) || ((itmdLeaf.getTau() > 0) && (itmdLeaf.getTau() < sourceLeaf.getTau() ) ) ) ) {
                                        definable = false;
                                    }
                                }
                            }
                        }
                        if (definable) {
                            // D(X) defineTo:6
                            CollectionsSynchronizedCollectionMonitor created = (CollectionsSynchronizedCollectionMonitor) sourceLeaf.clone();
                            matchedEntry = created;
                            matchedLastMap.putNode(wr_iter, created);
                            allSet.add(created); // clone monitor
                            created.node.monitors += 1;
                            // D(X) defineTo:7 for <col>
                            {
                                // InsertMonitor
                                Tuple3<MapOfMonitor<ICollectionsSynchronizedCollectionMonitor>, CollectionsSynchronizedCollectionMonitor_Set, CollectionsSynchronizedCollectionMonitor> node_col = CollectionsSynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col);
                                if ((node_col == null) ) {
                                    node_col = new Tuple3<>();
                                    CollectionsSynchronizedCollection_col_iter_Map.putNode(wr_col, node_col);
                                    node_col.setValue1(new MapOfMonitor<>(1) );
                                    node_col.setValue2(new CollectionsSynchronizedCollectionMonitor_Set() );
                                }
                                CollectionsSynchronizedCollectionMonitor_Set targetSet = node_col.getValue2();
                                targetSet.add(created);
                            }
                            // D(X) defineTo:7 for <iter>
                            {
                                // InsertMonitor
                                Tuple2<CollectionsSynchronizedCollectionMonitor_Set, ICollectionsSynchronizedCollectionMonitor> node_iter = CollectionsSynchronizedCollection_iter_Map.getNodeEquivalent(wr_iter);
                                if ((node_iter == null) ) {
                                    node_iter = new Tuple2<>();
                                    CollectionsSynchronizedCollection_iter_Map.putNode(wr_iter, node_iter);
                                    node_iter.setValue1(new CollectionsSynchronizedCollectionMonitor_Set() );
                                }
                                CollectionsSynchronizedCollectionMonitor_Set targetSet = node_iter.getValue1();
                                targetSet.add(created);
                            }
                        }
                    }
                }
                // D(X) main:6
                if ((matchedEntry == null) ) {
                    CollectionsSynchronizedCollectionDisableHolder holder = new CollectionsSynchronizedCollectionDisableHolder(-1);
                    matchedLastMap.putNode(wr_iter, holder);
                    matchedEntry = holder;
                }
                matchedEntry.setDisable(CollectionsSynchronizedCollection_timestamp++);
            }
            // D(X) main:8--9
            if (matchedEntry instanceof CollectionsSynchronizedCollectionMonitor) {
                CollectionsSynchronizedCollectionMonitor monitor = (CollectionsSynchronizedCollectionMonitor)matchedEntry;
                monitor.syncCreateIter(col, iter, event);

                if (!cachehit) {
                    col_iter_Map_cachekey_col = col;
                    col_iter_Map_cachekey_iter = iter;
                    col_iter_Map_cachevalue = matchedEntry;
                }
            }

        }

        lock.unlock();
    }

    public static void asyncCreateIter(Collection col, Iterator iter, int event) {
        while (!lock.tryLock()) {
            Thread.yield();
        }

//        System.out.println("asyncCreateIter - (" + System.identityHashCode(col) + ", " + System.identityHashCode(iter) + ") - event: " + event);

        if (activated) {
            CachedWeakReference wr_col = null;
            CachedWeakReference wr_iter = null;
            MapOfMonitor<ICollectionsSynchronizedCollectionMonitor> matchedLastMap = null;
            ICollectionsSynchronizedCollectionMonitor matchedEntry = null;
            boolean cachehit = false;
            if (((col == col_iter_Map_cachekey_col) && (iter == col_iter_Map_cachekey_iter) ) ) {
                matchedEntry = col_iter_Map_cachevalue;
                cachehit = true;
            }
            else {
                wr_col = new CachedWeakReference(col);
                wr_iter = new CachedWeakReference(iter);
                {
                    // FindOrCreateEntry
                    Tuple3<MapOfMonitor<ICollectionsSynchronizedCollectionMonitor>, CollectionsSynchronizedCollectionMonitor_Set, CollectionsSynchronizedCollectionMonitor> node_col = CollectionsSynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col);
                    if ((node_col == null) ) {
                        node_col = new Tuple3<>();
                        CollectionsSynchronizedCollection_col_iter_Map.putNode(wr_col, node_col);
                        node_col.setValue1(new MapOfMonitor<>(1) );
                        node_col.setValue2(new CollectionsSynchronizedCollectionMonitor_Set() );
                    }
                    MapOfMonitor<ICollectionsSynchronizedCollectionMonitor> itmdMap = node_col.getValue1();
                    matchedLastMap = itmdMap;
                    ICollectionsSynchronizedCollectionMonitor node_col_iter = node_col.getValue1() .getNodeEquivalent(wr_iter);
                    matchedEntry = node_col_iter;
                }
            }
            // D(X) main:1
            if ((matchedEntry == null) ) {
                if ((wr_col == null) ) {
                    wr_col = new CachedWeakReference(col);
                }
                if ((wr_iter == null) ) {
                    wr_iter = new CachedWeakReference(iter);
                }
                {
                    // D(X) createNewMonitorStates:4 when Dom(theta'') = <col>
                    CollectionsSynchronizedCollectionMonitor sourceLeaf = null;
                    {
                        // FindCode
                        Tuple3<MapOfMonitor<ICollectionsSynchronizedCollectionMonitor>, CollectionsSynchronizedCollectionMonitor_Set, CollectionsSynchronizedCollectionMonitor> node_col = CollectionsSynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col);
                        if ((node_col != null) ) {
                            CollectionsSynchronizedCollectionMonitor itmdLeaf = node_col.getValue3();
                            sourceLeaf = itmdLeaf;
                        }
                    }
                    if ((sourceLeaf != null) ) {
                        boolean definable = true;
                        // D(X) defineTo:1--5 for <col, iter>
                        if (definable) {
                            // FindCode
                            Tuple3<MapOfMonitor<ICollectionsSynchronizedCollectionMonitor>, CollectionsSynchronizedCollectionMonitor_Set, CollectionsSynchronizedCollectionMonitor> node_col = CollectionsSynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col);
                            if ((node_col != null) ) {
                                ICollectionsSynchronizedCollectionMonitor node_col_iter = node_col.getValue1() .getNodeEquivalent(wr_iter);
                                if ((node_col_iter != null) ) {
                                    if (((node_col_iter.getDisable() > sourceLeaf.getTau() ) || ((node_col_iter.getTau() > 0) && (node_col_iter.getTau() < sourceLeaf.getTau() ) ) ) ) {
                                        definable = false;
                                    }
                                }
                            }
                        }
                        // D(X) defineTo:1--5 for <iter>
                        if (definable) {
                            // FindCode
                            Tuple2<CollectionsSynchronizedCollectionMonitor_Set, ICollectionsSynchronizedCollectionMonitor> node_iter = CollectionsSynchronizedCollection_iter_Map.getNodeEquivalent(wr_iter);
                            if ((node_iter != null) ) {
                                ICollectionsSynchronizedCollectionMonitor itmdLeaf = node_iter.getValue2();
                                if ((itmdLeaf != null) ) {
                                    if (((itmdLeaf.getDisable() > sourceLeaf.getTau() ) || ((itmdLeaf.getTau() > 0) && (itmdLeaf.getTau() < sourceLeaf.getTau() ) ) ) ) {
                                        definable = false;
                                    }
                                }
                            }
                        }
                        if (definable) {
                            // D(X) defineTo:6
                            CollectionsSynchronizedCollectionMonitor created = (CollectionsSynchronizedCollectionMonitor) sourceLeaf.clone();
                            matchedEntry = created;
                            matchedLastMap.putNode(wr_iter, created);
                            created.node.monitors += 1;
                            // D(X) defineTo:7 for <col>
                            {
                                // InsertMonitor
                                Tuple3<MapOfMonitor<ICollectionsSynchronizedCollectionMonitor>, CollectionsSynchronizedCollectionMonitor_Set, CollectionsSynchronizedCollectionMonitor> node_col = CollectionsSynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col);
                                if ((node_col == null) ) {
                                    node_col = new Tuple3<>();
                                    CollectionsSynchronizedCollection_col_iter_Map.putNode(wr_col, node_col);
                                    node_col.setValue1(new MapOfMonitor<>(1) );
                                    node_col.setValue2(new CollectionsSynchronizedCollectionMonitor_Set() );
                                }
                                CollectionsSynchronizedCollectionMonitor_Set targetSet = node_col.getValue2();
                                targetSet.add(created);
                            }
                            // D(X) defineTo:7 for <iter>
                            {
                                // InsertMonitor
                                Tuple2<CollectionsSynchronizedCollectionMonitor_Set, ICollectionsSynchronizedCollectionMonitor> node_iter = CollectionsSynchronizedCollection_iter_Map.getNodeEquivalent(wr_iter);
                                if ((node_iter == null) ) {
                                    node_iter = new Tuple2<>();
                                    CollectionsSynchronizedCollection_iter_Map.putNode(wr_iter, node_iter);
                                    node_iter.setValue1(new CollectionsSynchronizedCollectionMonitor_Set() );
                                }
                                CollectionsSynchronizedCollectionMonitor_Set targetSet = node_iter.getValue1();
                                targetSet.add(created);
                            }
                        }
                    }
                }
                // D(X) main:6
                if ((matchedEntry == null) ) {
                    CollectionsSynchronizedCollectionDisableHolder holder = new CollectionsSynchronizedCollectionDisableHolder(-1);
                    matchedLastMap.putNode(wr_iter, holder);
                    matchedEntry = holder;
                }
                matchedEntry.setDisable(CollectionsSynchronizedCollection_timestamp++);
            }
            // D(X) main:8--9
            if (matchedEntry instanceof CollectionsSynchronizedCollectionMonitor) {
                CollectionsSynchronizedCollectionMonitor monitor = (CollectionsSynchronizedCollectionMonitor)matchedEntry;
                monitor.asyncCreateIter(col, iter, event);

                if (!cachehit) {
                    col_iter_Map_cachekey_col = col;
                    col_iter_Map_cachekey_iter = iter;
                    col_iter_Map_cachevalue = matchedEntry;
                }
            }

        }

        lock.unlock();
    }

    public static void accessIter(Iterator iter, int event) {
        while (!lock.tryLock()) {
            Thread.yield();
        }

//        System.out.println("accessIter - (" + System.identityHashCode(iter) + ") - event: " + event);

        if (activated) {
            CachedWeakReference wr_iter = null;
            Tuple2<CollectionsSynchronizedCollectionMonitor_Set, ICollectionsSynchronizedCollectionMonitor> matchedEntry = null;
            boolean cachehit = false;
            if ((iter == iter_Map_cachekey_iter) ) {
                matchedEntry = iter_Map_cachevalue;
                cachehit = true;
            }
            else {
                wr_iter = new CachedWeakReference(iter);
                {
                    // FindOrCreateEntry
                    Tuple2<CollectionsSynchronizedCollectionMonitor_Set, ICollectionsSynchronizedCollectionMonitor> node_iter = CollectionsSynchronizedCollection_iter_Map.getNodeEquivalent(wr_iter);
                    if ((node_iter == null) ) {
                        node_iter = new Tuple2<>();
                        CollectionsSynchronizedCollection_iter_Map.putNode(wr_iter, node_iter);
                        node_iter.setValue1(new CollectionsSynchronizedCollectionMonitor_Set() );
                    }
                    matchedEntry = node_iter;
                }
            }
            // D(X) main:1
            ICollectionsSynchronizedCollectionMonitor matchedLeaf = matchedEntry.getValue2();
            if ((matchedLeaf == null) ) {
                if ((wr_iter == null) ) {
                    wr_iter = new CachedWeakReference(iter);
                }
                // D(X) main:6
                ICollectionsSynchronizedCollectionMonitor disableUpdatedLeaf = matchedEntry.getValue2();
                if ((disableUpdatedLeaf == null) ) {
                    CollectionsSynchronizedCollectionDisableHolder holder = new CollectionsSynchronizedCollectionDisableHolder(-1);
                    matchedEntry.setValue2(holder);
                    disableUpdatedLeaf = holder;
                }
                disableUpdatedLeaf.setDisable(CollectionsSynchronizedCollection_timestamp++);
            }
            // D(X) main:8--9
            CollectionsSynchronizedCollectionMonitor_Set stateTransitionedSet = matchedEntry.getValue1();
            stateTransitionedSet.accessIter(iter, event);

            if (!cachehit) {
                iter_Map_cachekey_iter = iter;
                iter_Map_cachevalue = matchedEntry;
            }

        }

        lock.unlock();
    }

    public static void monitorSlices(String specName, MonitorManager monitorManager) {
        SpecializedSlicingAlgorithmUtil.monitorSlices(specName, monitorManager, trie.root);
    }
}

final class CollectionsSynchronizedCollectionMonitor_Set extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractMonitorSet<CollectionsSynchronizedCollectionMonitor> {
    CollectionsSynchronizedCollectionMonitor_Set(){
        this.size = 0;
        this.elements = new CollectionsSynchronizedCollectionMonitor[4];
    }

    void sync(Collection col, int event) {
        for(int i = 0; i < this.size; i++){
            elements[i].sync(col, event);
        }
    }
    void syncCreateIter(Collection col, Iterator iter, int event) {
        for(int i = 0; i < this.size; i++){
            elements[i].syncCreateIter(col, iter, event);
        }
    }

    void asyncCreateIter(Collection col, Iterator iter, int event) {
        for(int i = 0; i < this.size; i++){
            elements[i].asyncCreateIter(col, iter, event);
        }
    }

    void accessIter(Iterator iter, int event) {
        for(int i = 0; i < this.size; i++){
            elements[i].accessIter(iter, event);
        }
    }

    CollectionsSynchronizedCollectionMonitor[] getAll() {
        return elements;
    }
}

interface ICollectionsSynchronizedCollectionMonitor extends IMonitor, IDisableHolder {
}

class CollectionsSynchronizedCollectionDisableHolder extends DisableHolder implements ICollectionsSynchronizedCollectionMonitor {
    CollectionsSynchronizedCollectionDisableHolder(long tau) {
        super(tau);
    }

    @Override
    public final boolean isTerminated() {
        return false;
    }

    @Override
    public int getLastEvent() {
        return -1;
    }

    @Override
    public int getState() {
        return -1;
    }

}

class CollectionsSynchronizedCollectionMonitor extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractSynchronizedMonitor implements Cloneable, com.runtimeverification.rvmonitor.java.rt.RVMObject, ICollectionsSynchronizedCollectionMonitor {
    // Instead of calling it `monitor`, it is just the binding...
    protected Object clone() {
        try {
//            System.out.println("Monitor Cloning CollectionsSynchronizedCollectionMonitor");
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    int state;
    Trie.Node node;
    Collection col;

    CollectionsSynchronizedCollectionMonitor(long tau, Trie.Node node) {
        this.tau = tau;
        this.node = node;
        this.node.monitors += 1;
        state = 0;
    }

    @Override
    public final int getState() {
        return state;
    }

    private final long tau;
    private long disable = -1;

    @Override
    public final long getTau() {
        return this.tau;
    }

    @Override
    public final long getDisable() {
        return this.disable;
    }

    @Override
    public final void setDisable(long value) {
        this.disable = value;
    }

    final void sync(Collection col, int event) {
        this.col = col;
        see(event);
    }
    final void syncCreateIter(Collection col, Iterator iter, int event) {
        if (Thread.holdsLock(col)) {
            see(event);
        }
    }

    final void asyncCreateIter(Collection col, Iterator iter, int event) {
        if (!Thread.holdsLock(col)) {
            see(event);
        }
    }

    final void accessIter(Iterator iter, int event) {
        if (!Thread.holdsLock(this.col)) {
            see(event);
        }
    }

    private void see(int eID) {
        node.monitors -= 1;
        node = node.getNextNodeAfterSeeingEvent(eID);
        node.monitors += 1;
    }

    @Override
    protected final void terminateInternal(int idnum) {}
}
