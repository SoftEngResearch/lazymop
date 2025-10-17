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

final class Map_UnsafeIteratorMonitor_Set extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractMonitorSet<Map_UnsafeIteratorMonitor> {
	boolean matchMapUnsafeIteratorMonitorProp1;

	Map_UnsafeIteratorMonitor_Set(){
		this.size = 0;
		this.elements = new Map_UnsafeIteratorMonitor[4];
	}
	final void event_getset(Map m, Collection c) {
		this.matchMapUnsafeIteratorMonitorProp1 = false;
		int numAlive = 0 ;
		for(int i_1 = 0; i_1 < this.size; i_1++){
			Map_UnsafeIteratorMonitor monitor = this.elements[i_1];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final Map_UnsafeIteratorMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_getset(m, c);
				matchMapUnsafeIteratorMonitorProp1 |= monitorfinalMonitor.Map_UnsafeIteratorMonitor_Prop_1_Category_match;
				if(monitorfinalMonitor.Map_UnsafeIteratorMonitor_Prop_1_Category_match) {
					monitorfinalMonitor.Prop_1_handler_match();
				}
			}
		}
		for(int i_1 = numAlive; i_1 < this.size; i_1++){
			this.elements[i_1] = null;
		}
		size = numAlive;
	}
	final void event_getiter(Collection c, Iterator i) {
		this.matchMapUnsafeIteratorMonitorProp1 = false;
		int numAlive = 0 ;
		for(int i_1 = 0; i_1 < this.size; i_1++){
			Map_UnsafeIteratorMonitor monitor = this.elements[i_1];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final Map_UnsafeIteratorMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_getiter(c, i);
				matchMapUnsafeIteratorMonitorProp1 |= monitorfinalMonitor.Map_UnsafeIteratorMonitor_Prop_1_Category_match;
				if(monitorfinalMonitor.Map_UnsafeIteratorMonitor_Prop_1_Category_match) {
					monitorfinalMonitor.Prop_1_handler_match();
				}
			}
		}
		for(int i_1 = numAlive; i_1 < this.size; i_1++){
			this.elements[i_1] = null;
		}
		size = numAlive;
	}
	final void event_modifyMap(Map m) {
		this.matchMapUnsafeIteratorMonitorProp1 = false;
		int numAlive = 0 ;
		for(int i_1 = 0; i_1 < this.size; i_1++){
			Map_UnsafeIteratorMonitor monitor = this.elements[i_1];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final Map_UnsafeIteratorMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_modifyMap(m);
				matchMapUnsafeIteratorMonitorProp1 |= monitorfinalMonitor.Map_UnsafeIteratorMonitor_Prop_1_Category_match;
				if(monitorfinalMonitor.Map_UnsafeIteratorMonitor_Prop_1_Category_match) {
					monitorfinalMonitor.Prop_1_handler_match();
				}
			}
		}
		for(int i_1 = numAlive; i_1 < this.size; i_1++){
			this.elements[i_1] = null;
		}
		size = numAlive;
	}
	final void event_modifyCol(Collection c) {
		this.matchMapUnsafeIteratorMonitorProp1 = false;
		int numAlive = 0 ;
		for(int i_1 = 0; i_1 < this.size; i_1++){
			Map_UnsafeIteratorMonitor monitor = this.elements[i_1];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final Map_UnsafeIteratorMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_modifyCol(c);
				matchMapUnsafeIteratorMonitorProp1 |= monitorfinalMonitor.Map_UnsafeIteratorMonitor_Prop_1_Category_match;
				if(monitorfinalMonitor.Map_UnsafeIteratorMonitor_Prop_1_Category_match) {
					monitorfinalMonitor.Prop_1_handler_match();
				}
			}
		}
		for(int i_1 = numAlive; i_1 < this.size; i_1++){
			this.elements[i_1] = null;
		}
		size = numAlive;
	}
	final void event_useiter(Iterator i) {
		this.matchMapUnsafeIteratorMonitorProp1 = false;
		int numAlive = 0 ;
		for(int i_1 = 0; i_1 < this.size; i_1++){
			Map_UnsafeIteratorMonitor monitor = this.elements[i_1];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final Map_UnsafeIteratorMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_useiter(i);
				matchMapUnsafeIteratorMonitorProp1 |= monitorfinalMonitor.Map_UnsafeIteratorMonitor_Prop_1_Category_match;
				if(monitorfinalMonitor.Map_UnsafeIteratorMonitor_Prop_1_Category_match) {
					monitorfinalMonitor.Prop_1_handler_match();
				}
			}
		}
		for(int i_1 = numAlive; i_1 < this.size; i_1++){
			this.elements[i_1] = null;
		}
		size = numAlive;
	}
}

interface IMap_UnsafeIteratorMonitor extends IMonitor, IDisableHolder {
}

class Map_UnsafeIteratorDisableHolder extends DisableHolder implements IMap_UnsafeIteratorMonitor {
	Map_UnsafeIteratorDisableHolder(long tau) {
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

class Map_UnsafeIteratorMonitor extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractSynchronizedMonitor implements Cloneable, com.runtimeverification.rvmonitor.java.rt.RVMObject, IMap_UnsafeIteratorMonitor {
	protected Object clone() {
		try {
			Map_UnsafeIteratorMonitor ret = (Map_UnsafeIteratorMonitor) super.clone();
			return ret;
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	WeakReference Ref_c = null;
	WeakReference Ref_i = null;
	WeakReference Ref_m = null;
	int Prop_1_state;
	static final int Prop_1_transition_getset[] = {3, 5, 5, 5, 5, 5};;
	static final int Prop_1_transition_getiter[] = {5, 5, 5, 1, 5, 5};;
	static final int Prop_1_transition_modifyMap[] = {5, 4, 5, 3, 4, 5};;
	static final int Prop_1_transition_modifyCol[] = {5, 4, 5, 3, 4, 5};;
	static final int Prop_1_transition_useiter[] = {5, 1, 5, 5, 2, 5};;

	boolean Map_UnsafeIteratorMonitor_Prop_1_Category_match = false;

	Map_UnsafeIteratorMonitor(long tau, CachedWeakReference RVMRef_m) {
		this.tau = tau;
		Prop_1_state = 0;

		this.RVMRef_m = RVMRef_m;
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

	final boolean Prop_1_event_getset(Map m, Collection c) {
		Iterator i = null;
		if(Ref_i != null){
			i = (Iterator)Ref_i.get();
		}
		{
		}
		if(Ref_c == null){
			Ref_c = new WeakReference(c);
		}
		if(Ref_m == null){
			Ref_m = new WeakReference(m);
		}
		RVM_lastevent = 0;

		Prop_1_state = Prop_1_transition_getset[Prop_1_state];
		Map_UnsafeIteratorMonitor_Prop_1_Category_match = Prop_1_state == 2;
		return true;
	}

	final boolean Prop_1_event_getiter(Collection c, Iterator i) {
		Map m = null;
		if(Ref_m != null){
			m = (Map)Ref_m.get();
		}
		{
		}
		if(Ref_c == null){
			Ref_c = new WeakReference(c);
		}
		if(Ref_i == null){
			Ref_i = new WeakReference(i);
		}
		RVM_lastevent = 1;

		Prop_1_state = Prop_1_transition_getiter[Prop_1_state];
		Map_UnsafeIteratorMonitor_Prop_1_Category_match = Prop_1_state == 2;
		return true;
	}

	final boolean Prop_1_event_modifyMap(Map m) {
		Collection c = null;
		if(Ref_c != null){
			c = (Collection)Ref_c.get();
		}
		Iterator i = null;
		if(Ref_i != null){
			i = (Iterator)Ref_i.get();
		}
		{
		}
		if(Ref_m == null){
			Ref_m = new WeakReference(m);
		}
		RVM_lastevent = 2;

		Prop_1_state = Prop_1_transition_modifyMap[Prop_1_state];
		Map_UnsafeIteratorMonitor_Prop_1_Category_match = Prop_1_state == 2;
		return true;
	}

	final boolean Prop_1_event_modifyCol(Collection c) {
		Map m = null;
		if(Ref_m != null){
			m = (Map)Ref_m.get();
		}
		Iterator i = null;
		if(Ref_i != null){
			i = (Iterator)Ref_i.get();
		}
		{
		}
		if(Ref_c == null){
			Ref_c = new WeakReference(c);
		}
		RVM_lastevent = 3;

		Prop_1_state = Prop_1_transition_modifyCol[Prop_1_state];
		Map_UnsafeIteratorMonitor_Prop_1_Category_match = Prop_1_state == 2;
		return true;
	}

	final boolean Prop_1_event_useiter(Iterator i) {
		Map m = null;
		if(Ref_m != null){
			m = (Map)Ref_m.get();
		}
		Collection c = null;
		if(Ref_c != null){
			c = (Collection)Ref_c.get();
		}
		{
		}
		if(Ref_i == null){
			Ref_i = new WeakReference(i);
		}
		RVM_lastevent = 4;

		Prop_1_state = Prop_1_transition_useiter[Prop_1_state];
		Map_UnsafeIteratorMonitor_Prop_1_Category_match = Prop_1_state == 2;
		return true;
	}

	final void Prop_1_handler_match (){
		{
			RVMLogging.out.println(Level.CRITICAL, "Specification Map_UnsafeIterator has been violated on line " + com.runtimeverification.rvmonitor.java.rt.ViolationRecorder.getLineOfCode() + ". Documentation for this property can be found at http://runtimeverification.com/monitor/annotated-java/__properties/html/mop/Map_UnsafeIterator.html");
			RVMLogging.out.println(Level.CRITICAL, "The map was modified while an iteration over the set is in progress.");
		}

	}

	final void reset() {
		RVM_lastevent = -1;
		Prop_1_state = 0;
		Map_UnsafeIteratorMonitor_Prop_1_Category_match = false;
	}

	final CachedWeakReference RVMRef_m;
	// RVMRef_c was suppressed to reduce memory overhead
	// RVMRef_i was suppressed to reduce memory overhead

	//alive_parameters_0 = [Collection c, Iterator i]
	boolean alive_parameters_0 = true;
	//alive_parameters_1 = [Map m, Iterator i]
	boolean alive_parameters_1 = true;
	//alive_parameters_2 = [Iterator i]
	boolean alive_parameters_2 = true;

	@Override
	protected final void terminateInternal(int idnum) {
		switch(idnum){
			case 0:
			alive_parameters_1 = false;
			break;
			case 1:
			alive_parameters_0 = false;
			break;
			case 2:
			alive_parameters_0 = false;
			alive_parameters_1 = false;
			alive_parameters_2 = false;
			break;
		}
		switch(RVM_lastevent) {
			case -1:
			return;
			case 0:
			//getset
			//alive_c && alive_i
			if(!(alive_parameters_0)){
				RVM_terminated = true;
				return;
			}
			break;

			case 1:
			//getiter
			//alive_m && alive_i || alive_c && alive_i
			if(!(alive_parameters_1 || alive_parameters_0)){
				RVM_terminated = true;
				return;
			}
			break;

			case 2:
			//modifyMap
			//alive_i
			if(!(alive_parameters_2)){
				RVM_terminated = true;
				return;
			}
			break;

			case 3:
			//modifyCol
			//alive_i
			if(!(alive_parameters_2)){
				RVM_terminated = true;
				return;
			}
			break;

			case 4:
			//useiter
			//alive_m && alive_i || alive_c && alive_i
			if(!(alive_parameters_1 || alive_parameters_0)){
				RVM_terminated = true;
				return;
			}
			break;

		}
		return;
	}

	public static int getNumberOfEvents() {
		return 5;
	}

	public static int getNumberOfStates() {
		return 6;
	}

}

public final class Map_UnsafeIteratorRuntimeMonitor implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	private static boolean matchMapUnsafeIteratorMonitorProp1 = false;
	private static com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager Map_UnsafeIteratorMapManager;
	static {
		Map_UnsafeIteratorMapManager = new com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager();
		Map_UnsafeIteratorMapManager.start();
	}

	// Declarations for the Lock
	static final ReentrantLock Map_UnsafeIterator_RVMLock = new ReentrantLock();
	static final Condition Map_UnsafeIterator_RVMLock_cond = Map_UnsafeIterator_RVMLock.newCondition();

	// Declarations for Timestamps
	private static long Map_UnsafeIterator_timestamp = 1;

	private static boolean Map_UnsafeIterator_activated = false;

	// Declarations for Indexing Trees
	private static Object Map_UnsafeIterator_c_Map_cachekey_c;
	private static Tuple3<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> Map_UnsafeIterator_c_Map_cachevalue;
	private static Object Map_UnsafeIterator_c_i_Map_cachekey_c;
	private static Object Map_UnsafeIterator_c_i_Map_cachekey_i;
	private static Tuple2<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> Map_UnsafeIterator_c_i_Map_cachevalue;
	private static Object Map_UnsafeIterator_i_Map_cachekey_i;
	private static Tuple2<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> Map_UnsafeIterator_i_Map_cachevalue;
	private static Object Map_UnsafeIterator_m_Map_cachekey_m;
	private static Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> Map_UnsafeIterator_m_Map_cachevalue;
	private static Object Map_UnsafeIterator_m_c_Map_cachekey_c;
	private static Object Map_UnsafeIterator_m_c_Map_cachekey_m;
	private static Tuple3<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor> Map_UnsafeIterator_m_c_Map_cachevalue;
	private static Object Map_UnsafeIterator_m_c_i_Map_cachekey_c;
	private static Object Map_UnsafeIterator_m_c_i_Map_cachekey_i;
	private static Object Map_UnsafeIterator_m_c_i_Map_cachekey_m;
	private static IMap_UnsafeIteratorMonitor Map_UnsafeIterator_m_c_i_Map_cachevalue;
	private static final MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> Map_UnsafeIterator_i_Map = new MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>(2) ;
	private static final MapOfAll<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> Map_UnsafeIterator_c_i_Map = new MapOfAll<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>(1) ;
	private static final MapOfAll<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> Map_UnsafeIterator_m_c_i_Map = new MapOfAll<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>(0) ;
	private static Object Map_UnsafeIterator_c__To__m_c_Map_cachekey_c;
	private static Tuple2<Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor> Map_UnsafeIterator_c__To__m_c_Map_cachevalue;
	private static final MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor> Map_UnsafeIterator_c__To__m_c_Map = new MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>(1) ;

	public static int cleanUp() {
		int collected = 0;
		// indexing trees
		collected += Map_UnsafeIterator_i_Map.cleanUpUnnecessaryMappings();
		collected += Map_UnsafeIterator_c_i_Map.cleanUpUnnecessaryMappings();
		collected += Map_UnsafeIterator_m_c_i_Map.cleanUpUnnecessaryMappings();
		collected += Map_UnsafeIterator_c__To__m_c_Map.cleanUpUnnecessaryMappings();
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

	public static final void Map_UnsafeIterator_getsetEvent(Map m, Collection c) {
		Map_UnsafeIterator_activated = true;
		while (!Map_UnsafeIterator_RVMLock.tryLock()) {
			Thread.yield();
		}

		CachedWeakReference wr_c = null;
		CachedWeakReference wr_m = null;
		Tuple3<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor> matchedEntry = null;
		boolean cachehit = false;
		if (((c == Map_UnsafeIterator_m_c_Map_cachekey_c) && (m == Map_UnsafeIterator_m_c_Map_cachekey_m) ) ) {
			matchedEntry = Map_UnsafeIterator_m_c_Map_cachevalue;
			cachehit = true;
		}
		else {
			wr_m = new CachedWeakReference(m) ;
			wr_c = new CachedWeakReference(c) ;
			{
				// FindOrCreateEntry
				Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_m = Map_UnsafeIterator_m_c_i_Map.getNodeEquivalent(wr_m) ;
				if ((node_m == null) ) {
					node_m = new Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
					Map_UnsafeIterator_m_c_i_Map.putNode(wr_m, node_m) ;
					node_m.setValue1(new MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>(1) ) ;
					node_m.setValue2(new Map_UnsafeIteratorMonitor_Set() ) ;
				}
				Tuple3<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor> node_m_c = node_m.getValue1() .getNodeEquivalent(wr_c) ;
				if ((node_m_c == null) ) {
					node_m_c = new Tuple3<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>() ;
					node_m.getValue1() .putNode(wr_c, node_m_c) ;
					node_m_c.setValue1(new MapOfMonitor<IMap_UnsafeIteratorMonitor>(2) ) ;
					node_m_c.setValue2(new Map_UnsafeIteratorMonitor_Set() ) ;
				}
				matchedEntry = node_m_c;
			}
		}
		// D(X) main:1
		Map_UnsafeIteratorMonitor matchedLeaf = matchedEntry.getValue3() ;
		if ((matchedLeaf == null) ) {
			if ((wr_m == null) ) {
				wr_m = new CachedWeakReference(m) ;
			}
			if ((wr_c == null) ) {
				wr_c = new CachedWeakReference(c) ;
			}
			if ((matchedLeaf == null) ) {
				// D(X) main:4
				Map_UnsafeIteratorMonitor created = new Map_UnsafeIteratorMonitor(Map_UnsafeIterator_timestamp++, wr_m) ;
				matchedEntry.setValue3(created) ;
				Map_UnsafeIteratorMonitor_Set enclosingSet = matchedEntry.getValue2() ;
				enclosingSet.add(created) ;
				// D(X) defineNew:5 for <c>
				{
					// InsertMonitor
					Tuple3<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_c = Map_UnsafeIterator_c_i_Map.getNodeEquivalent(wr_c) ;
					if ((node_c == null) ) {
						node_c = new Tuple3<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
						Map_UnsafeIterator_c_i_Map.putNode(wr_c, node_c) ;
						node_c.setValue1(new MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>(1) ) ;
						node_c.setValue2(new Map_UnsafeIteratorMonitor_Set() ) ;
					}
					Map_UnsafeIteratorMonitor_Set targetSet = node_c.getValue2() ;
					targetSet.add(created) ;
				}
				// D(X) defineNew:5 for <m>
				{
					// InsertMonitor
					Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_m = Map_UnsafeIterator_m_c_i_Map.getNodeEquivalent(wr_m) ;
					if ((node_m == null) ) {
						node_m = new Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
						Map_UnsafeIterator_m_c_i_Map.putNode(wr_m, node_m) ;
						node_m.setValue1(new MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>(1) ) ;
						node_m.setValue2(new Map_UnsafeIteratorMonitor_Set() ) ;
					}
					Map_UnsafeIteratorMonitor_Set targetSet = node_m.getValue2() ;
					targetSet.add(created) ;
				}
				// D(X) defineNew:5 for <c-m, c>
				{
					// InsertMonitor
					Tuple2<Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor> node_c = Map_UnsafeIterator_c__To__m_c_Map.getNodeEquivalent(wr_c) ;
					if ((node_c == null) ) {
						node_c = new Tuple2<Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>() ;
						Map_UnsafeIterator_c__To__m_c_Map.putNode(wr_c, node_c) ;
						node_c.setValue1(new Map_UnsafeIteratorMonitor_Set() ) ;
					}
					Map_UnsafeIteratorMonitor_Set targetSet = node_c.getValue1() ;
					targetSet.add(created) ;
				}
			}
			// D(X) main:6
			Map_UnsafeIteratorMonitor disableUpdatedLeaf = matchedEntry.getValue3() ;
			disableUpdatedLeaf.setDisable(Map_UnsafeIterator_timestamp++) ;
		}
		// D(X) main:8--9
		Map_UnsafeIteratorMonitor_Set stateTransitionedSet = matchedEntry.getValue2() ;
		stateTransitionedSet.event_getset(m, c);
		matchMapUnsafeIteratorMonitorProp1 = stateTransitionedSet.matchMapUnsafeIteratorMonitorProp1;

		if ((cachehit == false) ) {
			Map_UnsafeIterator_m_c_Map_cachekey_c = c;
			Map_UnsafeIterator_m_c_Map_cachekey_m = m;
			Map_UnsafeIterator_m_c_Map_cachevalue = matchedEntry;
		}

		Map_UnsafeIterator_RVMLock.unlock();
	}

	public static final void Map_UnsafeIterator_getiterEvent(Collection c, Iterator i) {
		while (!Map_UnsafeIterator_RVMLock.tryLock()) {
			Thread.yield();
		}

		if (Map_UnsafeIterator_activated) {
			CachedWeakReference wr_c = null;
			CachedWeakReference wr_i = null;
			Tuple2<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> matchedEntry = null;
			boolean cachehit = false;
			if (((c == Map_UnsafeIterator_c_i_Map_cachekey_c) && (i == Map_UnsafeIterator_c_i_Map_cachekey_i) ) ) {
				matchedEntry = Map_UnsafeIterator_c_i_Map_cachevalue;
				cachehit = true;
			}
			else {
				wr_c = new CachedWeakReference(c) ;
				wr_i = new CachedWeakReference(i) ;
				{
					// FindOrCreateEntry
					Tuple3<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_c = Map_UnsafeIterator_c_i_Map.getNodeEquivalent(wr_c) ;
					if ((node_c == null) ) {
						node_c = new Tuple3<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
						Map_UnsafeIterator_c_i_Map.putNode(wr_c, node_c) ;
						node_c.setValue1(new MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>(1) ) ;
						node_c.setValue2(new Map_UnsafeIteratorMonitor_Set() ) ;
					}
					Tuple2<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_c_i = node_c.getValue1() .getNodeEquivalent(wr_i) ;
					if ((node_c_i == null) ) {
						node_c_i = new Tuple2<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
						node_c.getValue1() .putNode(wr_i, node_c_i) ;
						node_c_i.setValue1(new Map_UnsafeIteratorMonitor_Set() ) ;
					}
					matchedEntry = node_c_i;
				}
			}
			// D(X) main:1
			IMap_UnsafeIteratorMonitor matchedLeaf = matchedEntry.getValue2() ;
			if ((matchedLeaf == null) ) {
				if ((wr_c == null) ) {
					wr_c = new CachedWeakReference(c) ;
				}
				if ((wr_i == null) ) {
					wr_i = new CachedWeakReference(i) ;
				}
				{
					// D(X) createNewMonitorStates:4 when Dom(theta'') = <c>
					Map_UnsafeIteratorMonitor_Set sourceSet = null;
					{
						// FindCode
						Tuple2<Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor> node_c = Map_UnsafeIterator_c__To__m_c_Map.getNodeEquivalent(wr_c) ;
						if ((node_c != null) ) {
							Map_UnsafeIteratorMonitor_Set itmdSet = node_c.getValue1() ;
							sourceSet = itmdSet;
						}
					}
					if ((sourceSet != null) ) {
						int numalive = 0;
						int setlen = sourceSet.getSize() ;
						for (int ielem = 0; (ielem < setlen) ;++ielem) {
							Map_UnsafeIteratorMonitor sourceMonitor = sourceSet.get(ielem) ;
							if ((!sourceMonitor.isTerminated() && (sourceMonitor.RVMRef_m.get() != null) ) ) {
								sourceSet.set(numalive++, sourceMonitor) ;
								CachedWeakReference wr_m = sourceMonitor.RVMRef_m;
								MapOfMonitor<IMap_UnsafeIteratorMonitor> destLastMap = null;
								IMap_UnsafeIteratorMonitor destLeaf = null;
								{
									// FindOrCreate
									Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_m = Map_UnsafeIterator_m_c_i_Map.getNodeEquivalent(wr_m) ;
									if ((node_m == null) ) {
										node_m = new Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
										Map_UnsafeIterator_m_c_i_Map.putNode(wr_m, node_m) ;
										node_m.setValue1(new MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>(1) ) ;
										node_m.setValue2(new Map_UnsafeIteratorMonitor_Set() ) ;
									}
									Tuple3<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor> node_m_c = node_m.getValue1() .getNodeEquivalent(wr_c) ;
									if ((node_m_c == null) ) {
										node_m_c = new Tuple3<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>() ;
										node_m.getValue1() .putNode(wr_c, node_m_c) ;
										node_m_c.setValue1(new MapOfMonitor<IMap_UnsafeIteratorMonitor>(2) ) ;
										node_m_c.setValue2(new Map_UnsafeIteratorMonitor_Set() ) ;
									}
									MapOfMonitor<IMap_UnsafeIteratorMonitor> itmdMap = node_m_c.getValue1() ;
									destLastMap = itmdMap;
									IMap_UnsafeIteratorMonitor node_m_c_i = node_m_c.getValue1() .getNodeEquivalent(wr_i) ;
									destLeaf = node_m_c_i;
								}
								if (((destLeaf == null) || destLeaf instanceof Map_UnsafeIteratorDisableHolder) ) {
									boolean definable = true;
									// D(X) defineTo:1--5 for <c, i>
									if (definable) {
										// FindCode
										Tuple3<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_c = Map_UnsafeIterator_c_i_Map.getNodeEquivalent(wr_c) ;
										if ((node_c != null) ) {
											Tuple2<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_c_i = node_c.getValue1() .getNodeEquivalent(wr_i) ;
											if ((node_c_i != null) ) {
												IMap_UnsafeIteratorMonitor itmdLeaf = node_c_i.getValue2() ;
												if ((itmdLeaf != null) ) {
													if (((itmdLeaf.getDisable() > sourceMonitor.getTau() ) || ((itmdLeaf.getTau() > 0) && (itmdLeaf.getTau() < sourceMonitor.getTau() ) ) ) ) {
														definable = false;
													}
												}
											}
										}
									}
									// D(X) defineTo:1--5 for <i>
									if (definable) {
										// FindCode
										Tuple2<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_i = Map_UnsafeIterator_i_Map.getNodeEquivalent(wr_i) ;
										if ((node_i != null) ) {
											IMap_UnsafeIteratorMonitor itmdLeaf = node_i.getValue2() ;
											if ((itmdLeaf != null) ) {
												if (((itmdLeaf.getDisable() > sourceMonitor.getTau() ) || ((itmdLeaf.getTau() > 0) && (itmdLeaf.getTau() < sourceMonitor.getTau() ) ) ) ) {
													definable = false;
												}
											}
										}
									}
									// D(X) defineTo:1--5 for <m, c, i>
									if (definable) {
										// FindCode
										Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_m = Map_UnsafeIterator_m_c_i_Map.getNodeEquivalent(wr_m) ;
										if ((node_m != null) ) {
											Tuple3<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor> node_m_c = node_m.getValue1() .getNodeEquivalent(wr_c) ;
											if ((node_m_c != null) ) {
												IMap_UnsafeIteratorMonitor node_m_c_i = node_m_c.getValue1() .getNodeEquivalent(wr_i) ;
												if ((node_m_c_i != null) ) {
													if (((node_m_c_i.getDisable() > sourceMonitor.getTau() ) || ((node_m_c_i.getTau() > 0) && (node_m_c_i.getTau() < sourceMonitor.getTau() ) ) ) ) {
														definable = false;
													}
												}
											}
										}
									}
									if (definable) {
										// D(X) defineTo:6
										Map_UnsafeIteratorMonitor created = (Map_UnsafeIteratorMonitor)sourceMonitor.clone() ;
										destLastMap.putNode(wr_i, created) ;
										// D(X) defineTo:7 for <c>
										{
											// InsertMonitor
											Tuple3<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_c = Map_UnsafeIterator_c_i_Map.getNodeEquivalent(wr_c) ;
											if ((node_c == null) ) {
												node_c = new Tuple3<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
												Map_UnsafeIterator_c_i_Map.putNode(wr_c, node_c) ;
												node_c.setValue1(new MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>(1) ) ;
												node_c.setValue2(new Map_UnsafeIteratorMonitor_Set() ) ;
											}
											Map_UnsafeIteratorMonitor_Set targetSet = node_c.getValue2() ;
											targetSet.add(created) ;
										}
										// D(X) defineTo:7 for <c, i>
										{
											// InsertMonitor
											Tuple3<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_c = Map_UnsafeIterator_c_i_Map.getNodeEquivalent(wr_c) ;
											if ((node_c == null) ) {
												node_c = new Tuple3<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
												Map_UnsafeIterator_c_i_Map.putNode(wr_c, node_c) ;
												node_c.setValue1(new MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>(1) ) ;
												node_c.setValue2(new Map_UnsafeIteratorMonitor_Set() ) ;
											}
											Tuple2<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_c_i = node_c.getValue1() .getNodeEquivalent(wr_i) ;
											if ((node_c_i == null) ) {
												node_c_i = new Tuple2<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
												node_c.getValue1() .putNode(wr_i, node_c_i) ;
												node_c_i.setValue1(new Map_UnsafeIteratorMonitor_Set() ) ;
											}
											Map_UnsafeIteratorMonitor_Set targetSet = node_c_i.getValue1() ;
											targetSet.add(created) ;
										}
										// D(X) defineTo:7 for <i>
										{
											// InsertMonitor
											Tuple2<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_i = Map_UnsafeIterator_i_Map.getNodeEquivalent(wr_i) ;
											if ((node_i == null) ) {
												node_i = new Tuple2<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
												Map_UnsafeIterator_i_Map.putNode(wr_i, node_i) ;
												node_i.setValue1(new Map_UnsafeIteratorMonitor_Set() ) ;
											}
											Map_UnsafeIteratorMonitor_Set targetSet = node_i.getValue1() ;
											targetSet.add(created) ;
										}
										// D(X) defineTo:7 for <m>
										{
											// InsertMonitor
											Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_m = Map_UnsafeIterator_m_c_i_Map.getNodeEquivalent(wr_m) ;
											if ((node_m == null) ) {
												node_m = new Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
												Map_UnsafeIterator_m_c_i_Map.putNode(wr_m, node_m) ;
												node_m.setValue1(new MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>(1) ) ;
												node_m.setValue2(new Map_UnsafeIteratorMonitor_Set() ) ;
											}
											Map_UnsafeIteratorMonitor_Set targetSet = node_m.getValue2() ;
											targetSet.add(created) ;
										}
										// D(X) defineTo:7 for <m, c>
										{
											// InsertMonitor
											Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_m = Map_UnsafeIterator_m_c_i_Map.getNodeEquivalent(wr_m) ;
											if ((node_m == null) ) {
												node_m = new Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
												Map_UnsafeIterator_m_c_i_Map.putNode(wr_m, node_m) ;
												node_m.setValue1(new MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>(1) ) ;
												node_m.setValue2(new Map_UnsafeIteratorMonitor_Set() ) ;
											}
											Tuple3<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor> node_m_c = node_m.getValue1() .getNodeEquivalent(wr_c) ;
											if ((node_m_c == null) ) {
												node_m_c = new Tuple3<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>() ;
												node_m.getValue1() .putNode(wr_c, node_m_c) ;
												node_m_c.setValue1(new MapOfMonitor<IMap_UnsafeIteratorMonitor>(2) ) ;
												node_m_c.setValue2(new Map_UnsafeIteratorMonitor_Set() ) ;
											}
											Map_UnsafeIteratorMonitor_Set targetSet = node_m_c.getValue2() ;
											targetSet.add(created) ;
										}
									}
								}
							}
						}
						sourceSet.eraseRange(numalive) ;
					}
				}
				// D(X) main:6
				IMap_UnsafeIteratorMonitor disableUpdatedLeaf = matchedEntry.getValue2() ;
				if ((disableUpdatedLeaf == null) ) {
					Map_UnsafeIteratorDisableHolder holder = new Map_UnsafeIteratorDisableHolder(-1) ;
					matchedEntry.setValue2(holder) ;
					disableUpdatedLeaf = holder;
				}
				disableUpdatedLeaf.setDisable(Map_UnsafeIterator_timestamp++) ;
			}
			// D(X) main:8--9
			Map_UnsafeIteratorMonitor_Set stateTransitionedSet = matchedEntry.getValue1() ;
			stateTransitionedSet.event_getiter(c, i);
			matchMapUnsafeIteratorMonitorProp1 = stateTransitionedSet.matchMapUnsafeIteratorMonitorProp1;

			if ((cachehit == false) ) {
				Map_UnsafeIterator_c_i_Map_cachekey_c = c;
				Map_UnsafeIterator_c_i_Map_cachekey_i = i;
				Map_UnsafeIterator_c_i_Map_cachevalue = matchedEntry;
			}

		}

		Map_UnsafeIterator_RVMLock.unlock();
	}

	public static final void Map_UnsafeIterator_modifyMapEvent(Map m) {
		while (!Map_UnsafeIterator_RVMLock.tryLock()) {
			Thread.yield();
		}

		if (Map_UnsafeIterator_activated) {
			CachedWeakReference wr_m = null;
			Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> matchedEntry = null;
			boolean cachehit = false;
			if ((m == Map_UnsafeIterator_m_Map_cachekey_m) ) {
				matchedEntry = Map_UnsafeIterator_m_Map_cachevalue;
				cachehit = true;
			}
			else {
				wr_m = new CachedWeakReference(m) ;
				{
					// FindOrCreateEntry
					Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_m = Map_UnsafeIterator_m_c_i_Map.getNodeEquivalent(wr_m) ;
					if ((node_m == null) ) {
						node_m = new Tuple3<MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
						Map_UnsafeIterator_m_c_i_Map.putNode(wr_m, node_m) ;
						node_m.setValue1(new MapOfAll<MapOfMonitor<IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, Map_UnsafeIteratorMonitor>(1) ) ;
						node_m.setValue2(new Map_UnsafeIteratorMonitor_Set() ) ;
					}
					matchedEntry = node_m;
				}
			}
			// D(X) main:1
			IMap_UnsafeIteratorMonitor matchedLeaf = matchedEntry.getValue3() ;
			if ((matchedLeaf == null) ) {
				if ((wr_m == null) ) {
					wr_m = new CachedWeakReference(m) ;
				}
				// D(X) main:6
				IMap_UnsafeIteratorMonitor disableUpdatedLeaf = matchedEntry.getValue3() ;
				if ((disableUpdatedLeaf == null) ) {
					Map_UnsafeIteratorDisableHolder holder = new Map_UnsafeIteratorDisableHolder(-1) ;
					matchedEntry.setValue3(holder) ;
					disableUpdatedLeaf = holder;
				}
				disableUpdatedLeaf.setDisable(Map_UnsafeIterator_timestamp++) ;
			}
			// D(X) main:8--9
			Map_UnsafeIteratorMonitor_Set stateTransitionedSet = matchedEntry.getValue2() ;
			stateTransitionedSet.event_modifyMap(m);
			matchMapUnsafeIteratorMonitorProp1 = stateTransitionedSet.matchMapUnsafeIteratorMonitorProp1;

			if ((cachehit == false) ) {
				Map_UnsafeIterator_m_Map_cachekey_m = m;
				Map_UnsafeIterator_m_Map_cachevalue = matchedEntry;
			}

		}

		Map_UnsafeIterator_RVMLock.unlock();
	}

	public static final void Map_UnsafeIterator_modifyColEvent(Collection c) {
		while (!Map_UnsafeIterator_RVMLock.tryLock()) {
			Thread.yield();
		}

		if (Map_UnsafeIterator_activated) {
			CachedWeakReference wr_c = null;
			Tuple3<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> matchedEntry = null;
			boolean cachehit = false;
			if ((c == Map_UnsafeIterator_c_Map_cachekey_c) ) {
				matchedEntry = Map_UnsafeIterator_c_Map_cachevalue;
				cachehit = true;
			}
			else {
				wr_c = new CachedWeakReference(c) ;
				{
					// FindOrCreateEntry
					Tuple3<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_c = Map_UnsafeIterator_c_i_Map.getNodeEquivalent(wr_c) ;
					if ((node_c == null) ) {
						node_c = new Tuple3<MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>, Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
						Map_UnsafeIterator_c_i_Map.putNode(wr_c, node_c) ;
						node_c.setValue1(new MapOfSetMonitor<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>(1) ) ;
						node_c.setValue2(new Map_UnsafeIteratorMonitor_Set() ) ;
					}
					matchedEntry = node_c;
				}
			}
			// D(X) main:1
			IMap_UnsafeIteratorMonitor matchedLeaf = matchedEntry.getValue3() ;
			if ((matchedLeaf == null) ) {
				if ((wr_c == null) ) {
					wr_c = new CachedWeakReference(c) ;
				}
				// D(X) main:6
				IMap_UnsafeIteratorMonitor disableUpdatedLeaf = matchedEntry.getValue3() ;
				if ((disableUpdatedLeaf == null) ) {
					Map_UnsafeIteratorDisableHolder holder = new Map_UnsafeIteratorDisableHolder(-1) ;
					matchedEntry.setValue3(holder) ;
					disableUpdatedLeaf = holder;
				}
				disableUpdatedLeaf.setDisable(Map_UnsafeIterator_timestamp++) ;
			}
			// D(X) main:8--9
			Map_UnsafeIteratorMonitor_Set stateTransitionedSet = matchedEntry.getValue2() ;
			stateTransitionedSet.event_modifyCol(c);
			matchMapUnsafeIteratorMonitorProp1 = stateTransitionedSet.matchMapUnsafeIteratorMonitorProp1;

			if ((cachehit == false) ) {
				Map_UnsafeIterator_c_Map_cachekey_c = c;
				Map_UnsafeIterator_c_Map_cachevalue = matchedEntry;
			}

		}

		Map_UnsafeIterator_RVMLock.unlock();
	}

	public static final void Map_UnsafeIterator_useiterEvent(Iterator i) {
		while (!Map_UnsafeIterator_RVMLock.tryLock()) {
			Thread.yield();
		}

		if (Map_UnsafeIterator_activated) {
			CachedWeakReference wr_i = null;
			Tuple2<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> matchedEntry = null;
			boolean cachehit = false;
			if ((i == Map_UnsafeIterator_i_Map_cachekey_i) ) {
				matchedEntry = Map_UnsafeIterator_i_Map_cachevalue;
				cachehit = true;
			}
			else {
				wr_i = new CachedWeakReference(i) ;
				{
					// FindOrCreateEntry
					Tuple2<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor> node_i = Map_UnsafeIterator_i_Map.getNodeEquivalent(wr_i) ;
					if ((node_i == null) ) {
						node_i = new Tuple2<Map_UnsafeIteratorMonitor_Set, IMap_UnsafeIteratorMonitor>() ;
						Map_UnsafeIterator_i_Map.putNode(wr_i, node_i) ;
						node_i.setValue1(new Map_UnsafeIteratorMonitor_Set() ) ;
					}
					matchedEntry = node_i;
				}
			}
			// D(X) main:1
			IMap_UnsafeIteratorMonitor matchedLeaf = matchedEntry.getValue2() ;
			if ((matchedLeaf == null) ) {
				if ((wr_i == null) ) {
					wr_i = new CachedWeakReference(i) ;
				}
				// D(X) main:6
				IMap_UnsafeIteratorMonitor disableUpdatedLeaf = matchedEntry.getValue2() ;
				if ((disableUpdatedLeaf == null) ) {
					Map_UnsafeIteratorDisableHolder holder = new Map_UnsafeIteratorDisableHolder(-1) ;
					matchedEntry.setValue2(holder) ;
					disableUpdatedLeaf = holder;
				}
				disableUpdatedLeaf.setDisable(Map_UnsafeIterator_timestamp++) ;
			}
			// D(X) main:8--9
			Map_UnsafeIteratorMonitor_Set stateTransitionedSet = matchedEntry.getValue1() ;
			stateTransitionedSet.event_useiter(i);
			matchMapUnsafeIteratorMonitorProp1 = stateTransitionedSet.matchMapUnsafeIteratorMonitorProp1;

			if ((cachehit == false) ) {
				Map_UnsafeIterator_i_Map_cachekey_i = i;
				Map_UnsafeIterator_i_Map_cachevalue = matchedEntry;
			}

		}

		Map_UnsafeIterator_RVMLock.unlock();
	}

}
