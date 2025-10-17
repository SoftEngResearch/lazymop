package mop;
import java.util.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.io.*;
import java.lang.ref.*;
import com.runtimeverification.rvmonitor.java.rt.*;
import com.runtimeverification.rvmonitor.java.rt.ref.*;
import com.runtimeverification.rvmonitor.java.rt.table.*;
import com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractIndexingTree;
import com.runtimeverification.rvmonitor.java.rt.tablebase.SetEventDelegator;
import com.runtimeverification.rvmonitor.java.rt.tablebase.TableAdopter.Tuple2;
import com.runtimeverification.rvmonitor.java.rt.tablebase.TableAdopter.Tuple3;
import com.runtimeverification.rvmonitor.java.rt.tablebase.IDisableHolder;
import com.runtimeverification.rvmonitor.java.rt.tablebase.IMonitor;
import com.runtimeverification.rvmonitor.java.rt.tablebase.DisableHolder;
import com.runtimeverification.rvmonitor.java.rt.tablebase.TerminatedMonitorCleaner;

final class Iterator_HasNextMonitor_Set extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractMonitorSet<Iterator_HasNextMonitor> {
        boolean violationProp1;

        Iterator_HasNextMonitor_Set(){
                this.size = 0;
                this.elements = new Iterator_HasNextMonitor[4];
        }
        final void event_hasnexttrue(Iterator i, boolean b) {
                this.violationProp1 = false;
                int numAlive = 0 ;
                for(int i = 0; i < this.size; i++){
                        Iterator_HasNextMonitor monitor = this.elements[i];
                        if(!monitor.isTerminated()){
                                elements[numAlive] = monitor;
                                numAlive++;

                                final Iterator_HasNextMonitor monitorfinalMonitor = monitor;
                                monitor.Prop_1_event_hasnexttrue(i, b);
                                violationProp1 |= monitorfinalMonitor.Prop_1_Category_violation;
                                if(monitorfinalMonitor.Prop_1_Category_violation) {
                                        monitorfinalMonitor.Prop_1_handler_violation();
                                }
                        }
                }
                for(int i = numAlive; i < this.size; i++){
                        this.elements[i] = null;
                }
                size = numAlive;
        }
        final void event_hasnextfalse(Iterator i, boolean b) {
                this.violationProp1 = false;
                int numAlive = 0 ;
                for(int i = 0; i < this.size; i++){
                        Iterator_HasNextMonitor monitor = this.elements[i];
                        if(!monitor.isTerminated()){
                                elements[numAlive] = monitor;
                                numAlive++;

                                final Iterator_HasNextMonitor monitorfinalMonitor = monitor;
                                monitor.Prop_1_event_hasnextfalse(i, b);
                                violationProp1 |= monitorfinalMonitor.Prop_1_Category_violation;
                                if(monitorfinalMonitor.Prop_1_Category_violation) {
                                        monitorfinalMonitor.Prop_1_handler_violation();
                                }
                        }
                }
                for(int i = numAlive; i < this.size; i++){
                        this.elements[i] = null;
                }
                size = numAlive;
        }
        final void event_next(Iterator i) {
                this.violationProp1 = false;
                int numAlive = 0 ;
                for(int i = 0; i < this.size; i++){
                        Iterator_HasNextMonitor monitor = this.elements[i];
                        if(!monitor.isTerminated()){
                                elements[numAlive] = monitor;
                                numAlive++;

                                final Iterator_HasNextMonitor monitorfinalMonitor = monitor;
                                monitor.Prop_1_event_next(i);
                                violationProp1 |= monitorfinalMonitor.Prop_1_Category_violation;
                                if(monitorfinalMonitor.Prop_1_Category_violation) {
                                        monitorfinalMonitor.Prop_1_handler_violation();
                                }
                        }
                }
                for(int i = numAlive; i < this.size; i++){
                        this.elements[i] = null;
                }
                size = numAlive;
        }
}

class Iterator_HasNextMonitor extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractSynchronizedMonitor implements Cloneable, com.runtimeverification.rvmonitor.java.rt.RVMObject {
        protected Object clone() {
                try {
                        Iterator_HasNextMonitor ret = (Iterator_HasNextMonitor) super.clone();
                        return ret;
                }
                catch (CloneNotSupportedException e) {
                        throw new InternalError(e.toString());
                }
        }

        int Prop_1_state;
        static final int Prop_1_transition_hasnexttrue[] = {1, 1, 3, 3};;
        static final int Prop_1_transition_hasnextfalse[] = {0, 0, 3, 3};;
        static final int Prop_1_transition_next[] = {2, 0, 3, 3};;

        boolean Prop_1_Category_violation = false;

        Iterator_HasNextMonitor() {
                Prop_1_state = 0;

        }

        @Override
        public final int getState() {
                return Prop_1_state;
        }

        final boolean Prop_1_event_hasnexttrue(Iterator i, boolean b) {
                {
                        if ( ! (b) ) {
                                return false;
                        }
                        {
                        }
                }
                RVM_lastevent = 0;

                Prop_1_state = Prop_1_transition_hasnexttrue[Prop_1_state];
                Prop_1_Category_violation = Prop_1_state == 2;
                return true;
        }

        final boolean Prop_1_event_hasnextfalse(Iterator i, boolean b) {
                {
                        if ( ! (!b) ) {
                                return false;
                        }
                        {
                        }
                }
                RVM_lastevent = 1;

                Prop_1_state = Prop_1_transition_hasnextfalse[Prop_1_state];
                Prop_1_Category_violation = Prop_1_state == 2;
                return true;
        }

        final boolean Prop_1_event_next(Iterator i) {
                {
                }
                RVM_lastevent = 2;

                Prop_1_state = Prop_1_transition_next[Prop_1_state];
                Prop_1_Category_violation = Prop_1_state == 2;
                return true;
        }

        final void Prop_1_handler_violation (){
                {
                        RVMLogging.out.println(Level.WARNING, "Specification Iterator_HasNext has been violated on line " + com.runtimeverification.rvmonitor.java.rt.ViolationRecorder.getLineOfCode() + ". Documentation for this property can be found at http://runtimeverification.com/monitor/annotated-java/__properties/html/mop/Iterator_HasNext.html");
                        RVMLogging.out.println(Level.WARNING, "Iterator.hasNext() was not called before calling next().");
                }

        }

        final void reset() {
                RVM_lastevent = -1;
                Prop_1_state = 0;
                Prop_1_Category_violation = false;
        }

        // RVMRef_i was suppressed to reduce memory overhead

        //alive_parameters_0 = [Iterator i]
        boolean alive_parameters_0 = true;

        @Override
        protected final void terminateInternal(int idnum) {
                switch(idnum){
                        case 0:
                        alive_parameters_0 = false;
                        break;
                }
                switch(RVM_lastevent) {
                        case -1:
                        return;
                        case 0:
                        //hasnexttrue
                        //alive_i
                        if(!(alive_parameters_0)){
                                RVM_terminated = true;
                                return;
                        }
                        break;

                        case 1:
                        //hasnextfalse
                        //alive_i
                        if(!(alive_parameters_0)){
                                RVM_terminated = true;
                                return;
                        }
                        break;

                        case 2:
                        //next
                        //alive_i
                        if(!(alive_parameters_0)){
                                RVM_terminated = true;
                                return;
                        }
                        break;

                }
                return;
        }

        public static int getNumberOfEvents() {
                return 3;
        }

        public static int getNumberOfStates() {
                return 4;
        }

}

public final class Iterator_HasNextRuntimeMonitor implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
        private static boolean violationProp1 = false;
        private static com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager Iterator_HasNextMapManager;
        static {
                Iterator_HasNextMapManager = new com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager();
                Iterator_HasNextMapManager.start();
        }

        // Declarations for the Lock
        static final ReentrantLock Iterator_HasNext_RVMLock = new ReentrantLock();
        static final Condition Iterator_HasNext_RVMLock_cond = Iterator_HasNext_RVMLock.newCondition();

        private static boolean Iterator_HasNext_activated = false;

        // Declarations for Indexing Trees
        private static Object Iterator_HasNext_i_Map_cachekey_i;
        private static Iterator_HasNextMonitor Iterator_HasNext_i_Map_cachevalue;
        private static final MapOfMonitor<Iterator_HasNextMonitor> Iterator_HasNext_i_Map = new MapOfMonitor<Iterator_HasNextMonitor>(0) ;

        public static int cleanUp() {
                int collected = 0;
                // indexing trees
                collected += Iterator_HasNext_i_Map.cleanUpUnnecessaryMappings();
                return collected;
        }

        // Removing terminated monitors from partitioned sets
        static {
                TerminatedMonitorCleaner.start() ;
        }
        // Setting the behavior of the runtime library according to the compile-time option
        static {
                RuntimeOption.enableFineGrainedLock(false) ;
        }

        public static final void hasnexttrueEvent(Iterator i, boolean b) {
                Iterator_HasNext_activated = true;
                while (!Iterator_HasNext_RVMLock.tryLock()) {
                        Thread.yield();
                }

                CachedWeakReference wr_i = null;
                MapOfMonitor<Iterator_HasNextMonitor> matchedLastMap = null;
                Iterator_HasNextMonitor matchedEntry = null;
                boolean cachehit = false;
                if ((i == Iterator_HasNext_i_Map_cachekey_i) ) {
                        matchedEntry = Iterator_HasNext_i_Map_cachevalue;
                        cachehit = true;
                }
                else {
                        wr_i = new CachedWeakReference(i) ;
                        {
                                // FindOrCreateEntry
                                MapOfMonitor<Iterator_HasNextMonitor> itmdMap = Iterator_HasNext_i_Map;
                                matchedLastMap = itmdMap;
                                Iterator_HasNextMonitor node_i = Iterator_HasNext_i_Map.getNodeEquivalent(wr_i) ;
                                matchedEntry = node_i;
                        }
                }
                // D(X) main:1
                if ((matchedEntry == null) ) {
                        if ((wr_i == null) ) {
                                wr_i = new CachedWeakReference(i) ;
                        }
                        // D(X) main:4
                        Iterator_HasNextMonitor created = new Iterator_HasNextMonitor() ;
                        matchedEntry = created;
                        matchedLastMap.putNode(wr_i, created) ;
                }
                // D(X) main:8--9
                final Iterator_HasNextMonitor matchedEntryfinalMonitor = matchedEntry;
                matchedEntry.Prop_1_event_hasnexttrue(i, b);
                violationProp1 |= matchedEntryfinalMonitor.Prop_1_Category_violation;
                if(matchedEntryfinalMonitor.Prop_1_Category_violation) {
                        matchedEntryfinalMonitor.Prop_1_handler_violation();
                }

                if ((cachehit == false) ) {
                        Iterator_HasNext_i_Map_cachekey_i = i;
                        Iterator_HasNext_i_Map_cachevalue = matchedEntry;
                }

                Iterator_HasNext_RVMLock.unlock();
        }

        public static final void hasnextfalseEvent(Iterator i, boolean b) {
                Iterator_HasNext_activated = true;
                while (!Iterator_HasNext_RVMLock.tryLock()) {
                        Thread.yield();
                }

                CachedWeakReference wr_i = null;
                MapOfMonitor<Iterator_HasNextMonitor> matchedLastMap = null;
                Iterator_HasNextMonitor matchedEntry = null;
                boolean cachehit = false;
                if ((i == Iterator_HasNext_i_Map_cachekey_i) ) {
                        matchedEntry = Iterator_HasNext_i_Map_cachevalue;
                        cachehit = true;
                }
                else {
                        wr_i = new CachedWeakReference(i) ;
                        {
                                // FindOrCreateEntry
                                MapOfMonitor<Iterator_HasNextMonitor> itmdMap = Iterator_HasNext_i_Map;
                                matchedLastMap = itmdMap;
                                Iterator_HasNextMonitor node_i = Iterator_HasNext_i_Map.getNodeEquivalent(wr_i) ;
                                matchedEntry = node_i;
                        }
                }
                // D(X) main:1
                if ((matchedEntry == null) ) {
                        if ((wr_i == null) ) {
                                wr_i = new CachedWeakReference(i) ;
                        }
                        // D(X) main:4
                        Iterator_HasNextMonitor created = new Iterator_HasNextMonitor() ;
                        matchedEntry = created;
                        matchedLastMap.putNode(wr_i, created) ;
                }
                // D(X) main:8--9
                final Iterator_HasNextMonitor matchedEntryfinalMonitor = matchedEntry;
                matchedEntry.Prop_1_event_hasnextfalse(i, b);
                violationProp1 |= matchedEntryfinalMonitor.Prop_1_Category_violation;
                if(matchedEntryfinalMonitor.Prop_1_Category_violation) {
                        matchedEntryfinalMonitor.Prop_1_handler_violation();
                }

                if ((cachehit == false) ) {
                        Iterator_HasNext_i_Map_cachekey_i = i;
                        Iterator_HasNext_i_Map_cachevalue = matchedEntry;
                }

                Iterator_HasNext_RVMLock.unlock();
        }

        public static final void nextEvent(Iterator i) {
                Iterator_HasNext_activated = true;
                while (!Iterator_HasNext_RVMLock.tryLock()) {
                        Thread.yield();
                }

                CachedWeakReference wr_i = null;
                MapOfMonitor<Iterator_HasNextMonitor> matchedLastMap = null;
                Iterator_HasNextMonitor matchedEntry = null;
                boolean cachehit = false;
                if ((i == Iterator_HasNext_i_Map_cachekey_i) ) {
                        matchedEntry = Iterator_HasNext_i_Map_cachevalue;
                        cachehit = true;
                }
                else {
                        wr_i = new CachedWeakReference(i) ;
                        {
                                // FindOrCreateEntry
                                MapOfMonitor<Iterator_HasNextMonitor> itmdMap = Iterator_HasNext_i_Map;
                                matchedLastMap = itmdMap;
                                Iterator_HasNextMonitor node_i = Iterator_HasNext_i_Map.getNodeEquivalent(wr_i) ;
                                matchedEntry = node_i;
                        }
                }
                // D(X) main:1
                if ((matchedEntry == null) ) {
                        if ((wr_i == null) ) {
                                wr_i = new CachedWeakReference(i) ;
                        }
                        // D(X) main:4
                        Iterator_HasNextMonitor created = new Iterator_HasNextMonitor() ;
                        matchedEntry = created;
                        matchedLastMap.putNode(wr_i, created) ;
                }
                // D(X) main:8--9
                final Iterator_HasNextMonitor matchedEntryfinalMonitor = matchedEntry;
                matchedEntry.Prop_1_event_next(i);
                violationProp1 |= matchedEntryfinalMonitor.Prop_1_Category_violation;
                if(matchedEntryfinalMonitor.Prop_1_Category_violation) {
                        matchedEntryfinalMonitor.Prop_1_handler_violation();
                }

                if ((cachehit == false) ) {
                        Iterator_HasNext_i_Map_cachekey_i = i;
                        Iterator_HasNext_i_Map_cachevalue = matchedEntry;
                }

                Iterator_HasNext_RVMLock.unlock();
        }

}
