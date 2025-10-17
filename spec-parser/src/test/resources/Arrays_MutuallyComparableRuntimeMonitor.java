package mop;
import java.util.*;
import java.lang.*;
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

final class Arrays_MutuallyComparableMonitor_Set extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractMonitorSet<Arrays_MutuallyComparableMonitor> {

	Arrays_MutuallyComparableMonitor_Set(){
		this.size = 0;
		this.elements = new Arrays_MutuallyComparableMonitor[4];
	}
	final void event_invalid_sort(Object[] arr, Comparator comp) {
		int numAlive = 0 ;
		for(int i = 0; i < this.size; i++){
			Arrays_MutuallyComparableMonitor monitor = this.elements[i];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				monitor.event_invalid_sort(arr, comp);
			}
		}
		for(int i = numAlive; i < this.size; i++){
			this.elements[i] = null;
		}
		size = numAlive;
	}
}

class Arrays_MutuallyComparableMonitor extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractSynchronizedMonitor implements Cloneable, com.runtimeverification.rvmonitor.java.rt.RVMObject {
	protected Object clone() {
		try {
			Arrays_MutuallyComparableMonitor ret = (Arrays_MutuallyComparableMonitor) super.clone();
			return ret;
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	Arrays_MutuallyComparableMonitor(){
	}
	@Override
	public final int getState() {
		return -1;
	}

	final boolean event_invalid_sort(Object[] arr, Comparator comp) {
		RVM_lastevent = 0;
		{
			for (int i = 0; i < arr.length; ++i) {
				Object o1 = arr[i];
				for (int j = i + 1; j < arr.length; ++j) {
					Object o2 = arr[j];
					try {
						comp.compare(o1, o2);
						comp.compare(o2, o1);
					} catch (ClassCastException e) {
						RVMLogging.out.println(Level.CRITICAL, "Specification Arrays_MutuallyComparable has been violated on line " + com.runtimeverification.rvmonitor.java.rt.ViolationRecorder.getLineOfCode() + ". Documentation for this property can be found at http://runtimeverification.com/monitor/annotated-java/__properties/html/mop/Arrays_MutuallyComparable.html");
						RVMLogging.out.println(Level.CRITICAL, i + "-th element and " + j + "-th element are not comparable.");
					}
				}
			}
		}
		return true;
	}

	final void reset() {
		RVM_lastevent = -1;
	}

	@Override
	protected final void terminateInternal(int idnum) {
		switch(idnum){
		}
		switch(RVM_lastevent) {
			case -1:
			return;
			case 0:
			//invalid_sort
			return;
		}
		return;
	}

}

public final class Arrays_MutuallyComparableRuntimeMonitor implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	private static com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager Arrays_MutuallyComparableMapManager;
	static {
		Arrays_MutuallyComparableMapManager = new com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager();
		Arrays_MutuallyComparableMapManager.start();
	}

	// Declarations for the Lock
	static final ReentrantLock Arrays_MutuallyComparable_RVMLock = new ReentrantLock();
	static final Condition Arrays_MutuallyComparable_RVMLock_cond = Arrays_MutuallyComparable_RVMLock.newCondition();

	private static boolean Arrays_MutuallyComparable_activated = false;

	// Declarations for Indexing Trees
	private static final Arrays_MutuallyComparableMonitor Arrays_MutuallyComparable__Map = new Arrays_MutuallyComparableMonitor() ;

	public static int cleanUp() {
		int collected = 0;
		// indexing trees
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

	public static final void invalid_sortEvent(Object[] arr, Comparator comp) {
		Arrays_MutuallyComparable_activated = true;
		while (!Arrays_MutuallyComparable_RVMLock.tryLock()) {
			Thread.yield();
		}

		Arrays_MutuallyComparableMonitor matchedEntry = null;
		{
			// FindOrCreateEntry
			matchedEntry = Arrays_MutuallyComparable__Map;
		}
		// D(X) main:1
		if ((matchedEntry == null) ) {
			// D(X) main:4
			Arrays_MutuallyComparableMonitor created = new Arrays_MutuallyComparableMonitor() ;
			matchedEntry = created;
		}
		// D(X) main:8--9
		matchedEntry.event_invalid_sort(arr, comp);

		Arrays_MutuallyComparable_RVMLock.unlock();
	}

}
