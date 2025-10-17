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

final class Collections_SynchronizedCollectionMonitor_Set extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractMonitorSet<Collections_SynchronizedCollectionMonitor> {
	boolean matchProp1;

	Collections_SynchronizedCollectionMonitor_Set(){
		this.size = 0;
		this.elements = new Collections_SynchronizedCollectionMonitor[4];
	}
	final void event_sync(Collection col) {
		this.matchProp1 = false;
		int numAlive = 0 ;
		for(int i = 0; i < this.size; i++){
			Collections_SynchronizedCollectionMonitor monitor = this.elements[i];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final Collections_SynchronizedCollectionMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_sync(col);
				matchProp1 |= monitorfinalMonitor.Prop_1_Category_match;
				if(monitorfinalMonitor.Prop_1_Category_match) {
					monitorfinalMonitor.Prop_1_handler_match();
				}
			}
		}
		for(int i = numAlive; i < this.size; i++){
			this.elements[i] = null;
		}
		size = numAlive;
	}
	final void event_syncCreateIter(Collection col, Iterator iter) {
		this.matchProp1 = false;
		int numAlive = 0 ;
		for(int i = 0; i < this.size; i++){
			Collections_SynchronizedCollectionMonitor monitor = this.elements[i];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final Collections_SynchronizedCollectionMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_syncCreateIter(col, iter);
				matchProp1 |= monitorfinalMonitor.Prop_1_Category_match;
				if(monitorfinalMonitor.Prop_1_Category_match) {
					monitorfinalMonitor.Prop_1_handler_match();
				}
			}
		}
		for(int i = numAlive; i < this.size; i++){
			this.elements[i] = null;
		}
		size = numAlive;
	}
	final void event_asyncCreateIter(Collection col, Iterator iter) {
		this.matchProp1 = false;
		int numAlive = 0 ;
		for(int i = 0; i < this.size; i++){
			Collections_SynchronizedCollectionMonitor monitor = this.elements[i];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final Collections_SynchronizedCollectionMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_asyncCreateIter(col, iter);
				matchProp1 |= monitorfinalMonitor.Prop_1_Category_match;
				if(monitorfinalMonitor.Prop_1_Category_match) {
					monitorfinalMonitor.Prop_1_handler_match();
				}
			}
		}
		for(int i = numAlive; i < this.size; i++){
			this.elements[i] = null;
		}
		size = numAlive;
	}
	final void event_accessIter(Iterator iter) {
		this.matchProp1 = false;
		int numAlive = 0 ;
		for(int i = 0; i < this.size; i++){
			Collections_SynchronizedCollectionMonitor monitor = this.elements[i];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final Collections_SynchronizedCollectionMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_accessIter(iter);
				matchProp1 |= monitorfinalMonitor.Prop_1_Category_match;
				if(monitorfinalMonitor.Prop_1_Category_match) {
					monitorfinalMonitor.Prop_1_handler_match();
				}
			}
		}
		for(int i = numAlive; i < this.size; i++){
			this.elements[i] = null;
		}
		size = numAlive;
	}
}

interface ICollections_SynchronizedCollectionMonitor extends IMonitor, IDisableHolder {
}

class Collections_SynchronizedCollectionDisableHolder extends DisableHolder implements ICollections_SynchronizedCollectionMonitor {
	Collections_SynchronizedCollectionDisableHolder(long tau) {
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

class Collections_SynchronizedCollectionMonitor extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractSynchronizedMonitor implements Cloneable, com.runtimeverification.rvmonitor.java.rt.RVMObject, ICollections_SynchronizedCollectionMonitor {
	protected Object clone() {
		try {
			Collections_SynchronizedCollectionMonitor ret = (Collections_SynchronizedCollectionMonitor) super.clone();
			return ret;
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	Collection col;

	WeakReference Ref_col = null;
	WeakReference Ref_iter = null;
	int Prop_1_state;
	static final int Prop_1_transition_sync[] = {1, 4, 4, 4, 4};;
	static final int Prop_1_transition_syncCreateIter[] = {4, 2, 4, 4, 4};;
	static final int Prop_1_transition_asyncCreateIter[] = {4, 3, 4, 4, 4};;
	static final int Prop_1_transition_accessIter[] = {4, 4, 3, 4, 4};;

	boolean Prop_1_Category_match = false;

	Collections_SynchronizedCollectionMonitor(long tau) {
		this.tau = tau;
		Prop_1_state = 0;

	}

	@Override
	public final int getState() {
		return Prop_1_state;
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

	final boolean Prop_1_event_sync(Collection col) {
		Iterator iter = null;
		if(Ref_iter != null){
			iter = (Iterator)Ref_iter.get();
		}
		{
			this.col = col;
		}
		if(Ref_col == null){
			Ref_col = new WeakReference(col);
		}
		RVM_lastevent = 0;

		Prop_1_state = Prop_1_transition_sync[Prop_1_state];
		Prop_1_Category_match = Prop_1_state == 3;
		return true;
	}

	final boolean Prop_1_event_syncCreateIter(Collection col, Iterator iter) {
		{
			if ( ! (Thread.holdsLock(col)) ) {
				return false;
			}
			{
			}
		}
		if(Ref_col == null){
			Ref_col = new WeakReference(col);
		}
		if(Ref_iter == null){
			Ref_iter = new WeakReference(iter);
		}
		RVM_lastevent = 1;

		Prop_1_state = Prop_1_transition_syncCreateIter[Prop_1_state];
		Prop_1_Category_match = Prop_1_state == 3;
		return true;
	}

	final boolean Prop_1_event_asyncCreateIter(Collection col, Iterator iter) {
		{
			if ( ! (!Thread.holdsLock(col)) ) {
				return false;
			}
			{
			}
		}
		if(Ref_col == null){
			Ref_col = new WeakReference(col);
		}
		if(Ref_iter == null){
			Ref_iter = new WeakReference(iter);
		}
		RVM_lastevent = 2;

		Prop_1_state = Prop_1_transition_asyncCreateIter[Prop_1_state];
		Prop_1_Category_match = Prop_1_state == 3;
		return true;
	}

	final boolean Prop_1_event_accessIter(Iterator iter) {
		Collection col = null;
		if(Ref_col != null){
			col = (Collection)Ref_col.get();
		}
		{
			if ( ! (!Thread.holdsLock(this.col)) ) {
				return false;
			}
			{
			}
		}
		if(Ref_iter == null){
			Ref_iter = new WeakReference(iter);
		}
		RVM_lastevent = 3;

		Prop_1_state = Prop_1_transition_accessIter[Prop_1_state];
		Prop_1_Category_match = Prop_1_state == 3;
		return true;
	}

	final void Prop_1_handler_match (){
		{
			RVMLogging.out.println(Level.CRITICAL, "Specification Collections_SynchronizedCollection has been violated on line " + com.runtimeverification.rvmonitor.java.rt.ViolationRecorder.getLineOfCode() + ". Documentation for this property can be found at http://runtimeverification.com/monitor/annotated-java/__properties/html/mop/Collections_SynchronizedCollection.html");
			RVMLogging.out.println(Level.CRITICAL, "A synchronized collection was accessed in a thread-unsafe manner.");
		}

	}

	final void reset() {
		RVM_lastevent = -1;
		Prop_1_state = 0;
		Prop_1_Category_match = false;
	}

	// RVMRef_col was suppressed to reduce memory overhead
	// RVMRef_iter was suppressed to reduce memory overhead

	//alive_parameters_0 = [Collection col, Iterator iter]
	boolean alive_parameters_0 = true;
	//alive_parameters_1 = [Iterator iter]
	boolean alive_parameters_1 = true;

	@Override
	protected final void terminateInternal(int idnum) {
		switch(idnum){
			case 0:
			alive_parameters_0 = false;
			break;
			case 1:
			alive_parameters_0 = false;
			alive_parameters_1 = false;
			break;
		}
		switch(RVM_lastevent) {
			case -1:
			return;
			case 0:
			//sync
			//alive_col && alive_iter
			if(!(alive_parameters_0)){
				RVM_terminated = true;
				return;
			}
			break;

			case 1:
			//syncCreateIter
			//alive_iter
			if(!(alive_parameters_1)){
				RVM_terminated = true;
				return;
			}
			break;

			case 2:
			//asyncCreateIter
			return;
			case 3:
			//accessIter
			return;
		}
		return;
	}

	public static int getNumberOfEvents() {
		return 4;
	}

	public static int getNumberOfStates() {
		return 5;
	}

}

public final class Collections_SynchronizedCollectionRuntimeMonitor implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	private static boolean matchProp1 = false;
	private static com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager Collections_SynchronizedCollectionMapManager;
	static {
		Collections_SynchronizedCollectionMapManager = new com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager();
		Collections_SynchronizedCollectionMapManager.start();
	}

	// Declarations for the Lock
	static final ReentrantLock Collections_SynchronizedCollection_RVMLock = new ReentrantLock();
	static final Condition Collections_SynchronizedCollection_RVMLock_cond = Collections_SynchronizedCollection_RVMLock.newCondition();

	// Declarations for Timestamps
	private static long Collections_SynchronizedCollection_timestamp = 1;

	private static boolean Collections_SynchronizedCollection_activated = false;

	// Declarations for Indexing Trees
	private static Object Collections_SynchronizedCollection_col_Map_cachekey_col;
	private static Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor> Collections_SynchronizedCollection_col_Map_cachevalue;
	private static Object Collections_SynchronizedCollection_col_iter_Map_cachekey_col;
	private static Object Collections_SynchronizedCollection_col_iter_Map_cachekey_iter;
	private static ICollections_SynchronizedCollectionMonitor Collections_SynchronizedCollection_col_iter_Map_cachevalue;
	private static Object Collections_SynchronizedCollection_iter_Map_cachekey_iter;
	private static Tuple2<Collections_SynchronizedCollectionMonitor_Set, ICollections_SynchronizedCollectionMonitor> Collections_SynchronizedCollection_iter_Map_cachevalue;
	private static final MapOfSetMonitor<Collections_SynchronizedCollectionMonitor_Set, ICollections_SynchronizedCollectionMonitor> Collections_SynchronizedCollection_iter_Map = new MapOfSetMonitor<Collections_SynchronizedCollectionMonitor_Set, ICollections_SynchronizedCollectionMonitor>(1) ;
	private static final MapOfAll<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor> Collections_SynchronizedCollection_col_iter_Map = new MapOfAll<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor>(0) ;

	public static int cleanUp() {
		int collected = 0;
		// indexing trees
		collected += Collections_SynchronizedCollection_iter_Map.cleanUpUnnecessaryMappings();
		collected += Collections_SynchronizedCollection_col_iter_Map.cleanUpUnnecessaryMappings();
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

	public static final void syncEvent(Collection col) {
		Collections_SynchronizedCollection_activated = true;
		while (!Collections_SynchronizedCollection_RVMLock.tryLock()) {
			Thread.yield();
		}

		CachedWeakReference wr_col = null;
		Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor> matchedEntry = null;
		boolean cachehit = false;
		if ((col == Collections_SynchronizedCollection_col_Map_cachekey_col) ) {
			matchedEntry = Collections_SynchronizedCollection_col_Map_cachevalue;
			cachehit = true;
		}
		else {
			wr_col = new CachedWeakReference(col) ;
			{
				// FindOrCreateEntry
				Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor> node_col = Collections_SynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col) ;
				if ((node_col == null) ) {
					node_col = new Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor>() ;
					Collections_SynchronizedCollection_col_iter_Map.putNode(wr_col, node_col) ;
					node_col.setValue1(new MapOfMonitor<ICollections_SynchronizedCollectionMonitor>(1) ) ;
					node_col.setValue2(new Collections_SynchronizedCollectionMonitor_Set() ) ;
				}
				matchedEntry = node_col;
			}
		}
		// D(X) main:1
		Collections_SynchronizedCollectionMonitor matchedLeaf = matchedEntry.getValue3() ;
		if ((matchedLeaf == null) ) {
			if ((wr_col == null) ) {
				wr_col = new CachedWeakReference(col) ;
			}
			if ((matchedLeaf == null) ) {
				// D(X) main:4
				Collections_SynchronizedCollectionMonitor created = new Collections_SynchronizedCollectionMonitor(Collections_SynchronizedCollection_timestamp++) ;
				matchedEntry.setValue3(created) ;
				Collections_SynchronizedCollectionMonitor_Set enclosingSet = matchedEntry.getValue2() ;
				enclosingSet.add(created) ;
			}
			// D(X) main:6
			Collections_SynchronizedCollectionMonitor disableUpdatedLeaf = matchedEntry.getValue3() ;
			disableUpdatedLeaf.setDisable(Collections_SynchronizedCollection_timestamp++) ;
		}
		// D(X) main:8--9
		Collections_SynchronizedCollectionMonitor_Set stateTransitionedSet = matchedEntry.getValue2() ;
		stateTransitionedSet.event_sync(col);
		matchProp1 = stateTransitionedSet.matchProp1;

		if ((cachehit == false) ) {
			Collections_SynchronizedCollection_col_Map_cachekey_col = col;
			Collections_SynchronizedCollection_col_Map_cachevalue = matchedEntry;
		}

		Collections_SynchronizedCollection_RVMLock.unlock();
	}

	public static final void syncCreateIterEvent(Collection col, Iterator iter) {
		while (!Collections_SynchronizedCollection_RVMLock.tryLock()) {
			Thread.yield();
		}

		if (Collections_SynchronizedCollection_activated) {
			CachedWeakReference wr_col = null;
			CachedWeakReference wr_iter = null;
			MapOfMonitor<ICollections_SynchronizedCollectionMonitor> matchedLastMap = null;
			ICollections_SynchronizedCollectionMonitor matchedEntry = null;
			boolean cachehit = false;
			if (((col == Collections_SynchronizedCollection_col_iter_Map_cachekey_col) && (iter == Collections_SynchronizedCollection_col_iter_Map_cachekey_iter) ) ) {
				matchedEntry = Collections_SynchronizedCollection_col_iter_Map_cachevalue;
				cachehit = true;
			}
			else {
				wr_col = new CachedWeakReference(col) ;
				wr_iter = new CachedWeakReference(iter) ;
				{
					// FindOrCreateEntry
					Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor> node_col = Collections_SynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col) ;
					if ((node_col == null) ) {
						node_col = new Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor>() ;
						Collections_SynchronizedCollection_col_iter_Map.putNode(wr_col, node_col) ;
						node_col.setValue1(new MapOfMonitor<ICollections_SynchronizedCollectionMonitor>(1) ) ;
						node_col.setValue2(new Collections_SynchronizedCollectionMonitor_Set() ) ;
					}
					MapOfMonitor<ICollections_SynchronizedCollectionMonitor> itmdMap = node_col.getValue1() ;
					matchedLastMap = itmdMap;
					ICollections_SynchronizedCollectionMonitor node_col_iter = node_col.getValue1() .getNodeEquivalent(wr_iter) ;
					matchedEntry = node_col_iter;
				}
			}
			// D(X) main:1
			if ((matchedEntry == null) ) {
				if ((wr_col == null) ) {
					wr_col = new CachedWeakReference(col) ;
				}
				if ((wr_iter == null) ) {
					wr_iter = new CachedWeakReference(iter) ;
				}
				{
					// D(X) createNewMonitorStates:4 when Dom(theta'') = <col>
					Collections_SynchronizedCollectionMonitor sourceLeaf = null;
					{
						// FindCode
						Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor> node_col = Collections_SynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col) ;
						if ((node_col != null) ) {
							Collections_SynchronizedCollectionMonitor itmdLeaf = node_col.getValue3() ;
							sourceLeaf = itmdLeaf;
						}
					}
					if ((sourceLeaf != null) ) {
						boolean definable = true;
						// D(X) defineTo:1--5 for <col, iter>
						if (definable) {
							// FindCode
							Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor> node_col = Collections_SynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col) ;
							if ((node_col != null) ) {
								ICollections_SynchronizedCollectionMonitor node_col_iter = node_col.getValue1() .getNodeEquivalent(wr_iter) ;
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
							Tuple2<Collections_SynchronizedCollectionMonitor_Set, ICollections_SynchronizedCollectionMonitor> node_iter = Collections_SynchronizedCollection_iter_Map.getNodeEquivalent(wr_iter) ;
							if ((node_iter != null) ) {
								ICollections_SynchronizedCollectionMonitor itmdLeaf = node_iter.getValue2() ;
								if ((itmdLeaf != null) ) {
									if (((itmdLeaf.getDisable() > sourceLeaf.getTau() ) || ((itmdLeaf.getTau() > 0) && (itmdLeaf.getTau() < sourceLeaf.getTau() ) ) ) ) {
										definable = false;
									}
								}
							}
						}
						if (definable) {
							// D(X) defineTo:6
							Collections_SynchronizedCollectionMonitor created = (Collections_SynchronizedCollectionMonitor)sourceLeaf.clone() ;
							matchedEntry = created;
							matchedLastMap.putNode(wr_iter, created) ;
							// D(X) defineTo:7 for <col>
							{
								// InsertMonitor
								Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor> node_col = Collections_SynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col) ;
								if ((node_col == null) ) {
									node_col = new Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor>() ;
									Collections_SynchronizedCollection_col_iter_Map.putNode(wr_col, node_col) ;
									node_col.setValue1(new MapOfMonitor<ICollections_SynchronizedCollectionMonitor>(1) ) ;
									node_col.setValue2(new Collections_SynchronizedCollectionMonitor_Set() ) ;
								}
								Collections_SynchronizedCollectionMonitor_Set targetSet = node_col.getValue2() ;
								targetSet.add(created) ;
							}
							// D(X) defineTo:7 for <iter>
							{
								// InsertMonitor
								Tuple2<Collections_SynchronizedCollectionMonitor_Set, ICollections_SynchronizedCollectionMonitor> node_iter = Collections_SynchronizedCollection_iter_Map.getNodeEquivalent(wr_iter) ;
								if ((node_iter == null) ) {
									node_iter = new Tuple2<Collections_SynchronizedCollectionMonitor_Set, ICollections_SynchronizedCollectionMonitor>() ;
									Collections_SynchronizedCollection_iter_Map.putNode(wr_iter, node_iter) ;
									node_iter.setValue1(new Collections_SynchronizedCollectionMonitor_Set() ) ;
								}
								Collections_SynchronizedCollectionMonitor_Set targetSet = node_iter.getValue1() ;
								targetSet.add(created) ;
							}
						}
					}
				}
				// D(X) main:6
				if ((matchedEntry == null) ) {
					Collections_SynchronizedCollectionDisableHolder holder = new Collections_SynchronizedCollectionDisableHolder(-1) ;
					matchedLastMap.putNode(wr_iter, holder) ;
					matchedEntry = holder;
				}
				matchedEntry.setDisable(Collections_SynchronizedCollection_timestamp++) ;
			}
			// D(X) main:8--9
			if (matchedEntry instanceof Collections_SynchronizedCollectionMonitor) {
				Collections_SynchronizedCollectionMonitor monitor = (Collections_SynchronizedCollectionMonitor)matchedEntry;
				final Collections_SynchronizedCollectionMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_syncCreateIter(col, iter);
				matchProp1 |= monitorfinalMonitor.Prop_1_Category_match;
				if(monitorfinalMonitor.Prop_1_Category_match) {
					monitorfinalMonitor.Prop_1_handler_match();
				}

				if ((cachehit == false) ) {
					Collections_SynchronizedCollection_col_iter_Map_cachekey_col = col;
					Collections_SynchronizedCollection_col_iter_Map_cachekey_iter = iter;
					Collections_SynchronizedCollection_col_iter_Map_cachevalue = matchedEntry;
				}
			}

		}

		Collections_SynchronizedCollection_RVMLock.unlock();
	}

	public static final void asyncCreateIterEvent(Collection col, Iterator iter) {
		while (!Collections_SynchronizedCollection_RVMLock.tryLock()) {
			Thread.yield();
		}

		if (Collections_SynchronizedCollection_activated) {
			CachedWeakReference wr_col = null;
			CachedWeakReference wr_iter = null;
			MapOfMonitor<ICollections_SynchronizedCollectionMonitor> matchedLastMap = null;
			ICollections_SynchronizedCollectionMonitor matchedEntry = null;
			boolean cachehit = false;
			if (((col == Collections_SynchronizedCollection_col_iter_Map_cachekey_col) && (iter == Collections_SynchronizedCollection_col_iter_Map_cachekey_iter) ) ) {
				matchedEntry = Collections_SynchronizedCollection_col_iter_Map_cachevalue;
				cachehit = true;
			}
			else {
				wr_col = new CachedWeakReference(col) ;
				wr_iter = new CachedWeakReference(iter) ;
				{
					// FindOrCreateEntry
					Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor> node_col = Collections_SynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col) ;
					if ((node_col == null) ) {
						node_col = new Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor>() ;
						Collections_SynchronizedCollection_col_iter_Map.putNode(wr_col, node_col) ;
						node_col.setValue1(new MapOfMonitor<ICollections_SynchronizedCollectionMonitor>(1) ) ;
						node_col.setValue2(new Collections_SynchronizedCollectionMonitor_Set() ) ;
					}
					MapOfMonitor<ICollections_SynchronizedCollectionMonitor> itmdMap = node_col.getValue1() ;
					matchedLastMap = itmdMap;
					ICollections_SynchronizedCollectionMonitor node_col_iter = node_col.getValue1() .getNodeEquivalent(wr_iter) ;
					matchedEntry = node_col_iter;
				}
			}
			// D(X) main:1
			if ((matchedEntry == null) ) {
				if ((wr_col == null) ) {
					wr_col = new CachedWeakReference(col) ;
				}
				if ((wr_iter == null) ) {
					wr_iter = new CachedWeakReference(iter) ;
				}
				{
					// D(X) createNewMonitorStates:4 when Dom(theta'') = <col>
					Collections_SynchronizedCollectionMonitor sourceLeaf = null;
					{
						// FindCode
						Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor> node_col = Collections_SynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col) ;
						if ((node_col != null) ) {
							Collections_SynchronizedCollectionMonitor itmdLeaf = node_col.getValue3() ;
							sourceLeaf = itmdLeaf;
						}
					}
					if ((sourceLeaf != null) ) {
						boolean definable = true;
						// D(X) defineTo:1--5 for <col, iter>
						if (definable) {
							// FindCode
							Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor> node_col = Collections_SynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col) ;
							if ((node_col != null) ) {
								ICollections_SynchronizedCollectionMonitor node_col_iter = node_col.getValue1() .getNodeEquivalent(wr_iter) ;
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
							Tuple2<Collections_SynchronizedCollectionMonitor_Set, ICollections_SynchronizedCollectionMonitor> node_iter = Collections_SynchronizedCollection_iter_Map.getNodeEquivalent(wr_iter) ;
							if ((node_iter != null) ) {
								ICollections_SynchronizedCollectionMonitor itmdLeaf = node_iter.getValue2() ;
								if ((itmdLeaf != null) ) {
									if (((itmdLeaf.getDisable() > sourceLeaf.getTau() ) || ((itmdLeaf.getTau() > 0) && (itmdLeaf.getTau() < sourceLeaf.getTau() ) ) ) ) {
										definable = false;
									}
								}
							}
						}
						if (definable) {
							// D(X) defineTo:6
							Collections_SynchronizedCollectionMonitor created = (Collections_SynchronizedCollectionMonitor)sourceLeaf.clone() ;
							matchedEntry = created;
							matchedLastMap.putNode(wr_iter, created) ;
							// D(X) defineTo:7 for <col>
							{
								// InsertMonitor
								Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor> node_col = Collections_SynchronizedCollection_col_iter_Map.getNodeEquivalent(wr_col) ;
								if ((node_col == null) ) {
									node_col = new Tuple3<MapOfMonitor<ICollections_SynchronizedCollectionMonitor>, Collections_SynchronizedCollectionMonitor_Set, Collections_SynchronizedCollectionMonitor>() ;
									Collections_SynchronizedCollection_col_iter_Map.putNode(wr_col, node_col) ;
									node_col.setValue1(new MapOfMonitor<ICollections_SynchronizedCollectionMonitor>(1) ) ;
									node_col.setValue2(new Collections_SynchronizedCollectionMonitor_Set() ) ;
								}
								Collections_SynchronizedCollectionMonitor_Set targetSet = node_col.getValue2() ;
								targetSet.add(created) ;
							}
							// D(X) defineTo:7 for <iter>
							{
								// InsertMonitor
								Tuple2<Collections_SynchronizedCollectionMonitor_Set, ICollections_SynchronizedCollectionMonitor> node_iter = Collections_SynchronizedCollection_iter_Map.getNodeEquivalent(wr_iter) ;
								if ((node_iter == null) ) {
									node_iter = new Tuple2<Collections_SynchronizedCollectionMonitor_Set, ICollections_SynchronizedCollectionMonitor>() ;
									Collections_SynchronizedCollection_iter_Map.putNode(wr_iter, node_iter) ;
									node_iter.setValue1(new Collections_SynchronizedCollectionMonitor_Set() ) ;
								}
								Collections_SynchronizedCollectionMonitor_Set targetSet = node_iter.getValue1() ;
								targetSet.add(created) ;
							}
						}
					}
				}
				// D(X) main:6
				if ((matchedEntry == null) ) {
					Collections_SynchronizedCollectionDisableHolder holder = new Collections_SynchronizedCollectionDisableHolder(-1) ;
					matchedLastMap.putNode(wr_iter, holder) ;
					matchedEntry = holder;
				}
				matchedEntry.setDisable(Collections_SynchronizedCollection_timestamp++) ;
			}
			// D(X) main:8--9
			if (matchedEntry instanceof Collections_SynchronizedCollectionMonitor) {
				Collections_SynchronizedCollectionMonitor monitor = (Collections_SynchronizedCollectionMonitor)matchedEntry;
				final Collections_SynchronizedCollectionMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_asyncCreateIter(col, iter);
				matchProp1 |= monitorfinalMonitor.Prop_1_Category_match;
				if(monitorfinalMonitor.Prop_1_Category_match) {
					monitorfinalMonitor.Prop_1_handler_match();
				}

				if ((cachehit == false) ) {
					Collections_SynchronizedCollection_col_iter_Map_cachekey_col = col;
					Collections_SynchronizedCollection_col_iter_Map_cachekey_iter = iter;
					Collections_SynchronizedCollection_col_iter_Map_cachevalue = matchedEntry;
				}
			}

		}

		Collections_SynchronizedCollection_RVMLock.unlock();
	}

	public static final void accessIterEvent(Iterator iter) {
		while (!Collections_SynchronizedCollection_RVMLock.tryLock()) {
			Thread.yield();
		}

		if (Collections_SynchronizedCollection_activated) {
			CachedWeakReference wr_iter = null;
			Tuple2<Collections_SynchronizedCollectionMonitor_Set, ICollections_SynchronizedCollectionMonitor> matchedEntry = null;
			boolean cachehit = false;
			if ((iter == Collections_SynchronizedCollection_iter_Map_cachekey_iter) ) {
				matchedEntry = Collections_SynchronizedCollection_iter_Map_cachevalue;
				cachehit = true;
			}
			else {
				wr_iter = new CachedWeakReference(iter) ;
				{
					// FindOrCreateEntry
					Tuple2<Collections_SynchronizedCollectionMonitor_Set, ICollections_SynchronizedCollectionMonitor> node_iter = Collections_SynchronizedCollection_iter_Map.getNodeEquivalent(wr_iter) ;
					if ((node_iter == null) ) {
						node_iter = new Tuple2<Collections_SynchronizedCollectionMonitor_Set, ICollections_SynchronizedCollectionMonitor>() ;
						Collections_SynchronizedCollection_iter_Map.putNode(wr_iter, node_iter) ;
						node_iter.setValue1(new Collections_SynchronizedCollectionMonitor_Set() ) ;
					}
					matchedEntry = node_iter;
				}
			}
			// D(X) main:1
			ICollections_SynchronizedCollectionMonitor matchedLeaf = matchedEntry.getValue2() ;
			if ((matchedLeaf == null) ) {
				if ((wr_iter == null) ) {
					wr_iter = new CachedWeakReference(iter) ;
				}
				// D(X) main:6
				ICollections_SynchronizedCollectionMonitor disableUpdatedLeaf = matchedEntry.getValue2() ;
				if ((disableUpdatedLeaf == null) ) {
					Collections_SynchronizedCollectionDisableHolder holder = new Collections_SynchronizedCollectionDisableHolder(-1) ;
					matchedEntry.setValue2(holder) ;
					disableUpdatedLeaf = holder;
				}
				disableUpdatedLeaf.setDisable(Collections_SynchronizedCollection_timestamp++) ;
			}
			// D(X) main:8--9
			Collections_SynchronizedCollectionMonitor_Set stateTransitionedSet = matchedEntry.getValue1() ;
			stateTransitionedSet.event_accessIter(iter);
			matchProp1 = stateTransitionedSet.matchProp1;

			if ((cachehit == false) ) {
				Collections_SynchronizedCollection_iter_Map_cachekey_iter = iter;
				Collections_SynchronizedCollection_iter_Map_cachevalue = matchedEntry;
			}

		}

		Collections_SynchronizedCollection_RVMLock.unlock();
	}

}
