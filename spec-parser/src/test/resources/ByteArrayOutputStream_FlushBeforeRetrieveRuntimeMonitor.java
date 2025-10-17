package mop;
import java.io.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;
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

final class ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractMonitorSet<ByteArrayOutputStream_FlushBeforeRetrieveMonitor> {
	boolean failProp1;

	ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set(){
		this.size = 0;
		this.elements = new ByteArrayOutputStream_FlushBeforeRetrieveMonitor[4];
	}
	final void event_outputstreaminit(ByteArrayOutputStream b, OutputStream o) {
		this.failProp1 = false;
		int numAlive = 0 ;
		for(int i = 0; i < this.size; i++){
			ByteArrayOutputStream_FlushBeforeRetrieveMonitor monitor = this.elements[i];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final ByteArrayOutputStream_FlushBeforeRetrieveMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_outputstreaminit(b, o);
				failProp1 |= monitorfinalMonitor.Prop_1_Category_fail;
				if(monitorfinalMonitor.Prop_1_Category_fail) {
					monitorfinalMonitor.Prop_1_handler_fail();
				}
			}
		}
		for(int i = numAlive; i < this.size; i++){
			this.elements[i] = null;
		}
		size = numAlive;
	}
	final void event_write(OutputStream o) {
		this.failProp1 = false;
		int numAlive = 0 ;
		for(int i = 0; i < this.size; i++){
			ByteArrayOutputStream_FlushBeforeRetrieveMonitor monitor = this.elements[i];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final ByteArrayOutputStream_FlushBeforeRetrieveMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_write(o);
				failProp1 |= monitorfinalMonitor.Prop_1_Category_fail;
				if(monitorfinalMonitor.Prop_1_Category_fail) {
					monitorfinalMonitor.Prop_1_handler_fail();
				}
			}
		}
		for(int i = numAlive; i < this.size; i++){
			this.elements[i] = null;
		}
		size = numAlive;
	}
	final void event_flush(OutputStream o) {
		this.failProp1 = false;
		int numAlive = 0 ;
		for(int i = 0; i < this.size; i++){
			ByteArrayOutputStream_FlushBeforeRetrieveMonitor monitor = this.elements[i];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final ByteArrayOutputStream_FlushBeforeRetrieveMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_flush(o);
				failProp1 |= monitorfinalMonitor.Prop_1_Category_fail;
				if(monitorfinalMonitor.Prop_1_Category_fail) {
					monitorfinalMonitor.Prop_1_handler_fail();
				}
			}
		}
		for(int i = numAlive; i < this.size; i++){
			this.elements[i] = null;
		}
		size = numAlive;
	}
	final void event_close(OutputStream o) {
		this.failProp1 = false;
		int numAlive = 0 ;
		for(int i = 0; i < this.size; i++){
			ByteArrayOutputStream_FlushBeforeRetrieveMonitor monitor = this.elements[i];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final ByteArrayOutputStream_FlushBeforeRetrieveMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_close(o);
				failProp1 |= monitorfinalMonitor.Prop_1_Category_fail;
				if(monitorfinalMonitor.Prop_1_Category_fail) {
					monitorfinalMonitor.Prop_1_handler_fail();
				}
			}
		}
		for(int i = numAlive; i < this.size; i++){
			this.elements[i] = null;
		}
		size = numAlive;
	}
	final void event_tobytearray(ByteArrayOutputStream b) {
		this.failProp1 = false;
		int numAlive = 0 ;
		for(int i = 0; i < this.size; i++){
			ByteArrayOutputStream_FlushBeforeRetrieveMonitor monitor = this.elements[i];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final ByteArrayOutputStream_FlushBeforeRetrieveMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_tobytearray(b);
				failProp1 |= monitorfinalMonitor.Prop_1_Category_fail;
				if(monitorfinalMonitor.Prop_1_Category_fail) {
					monitorfinalMonitor.Prop_1_handler_fail();
				}
			}
		}
		for(int i = numAlive; i < this.size; i++){
			this.elements[i] = null;
		}
		size = numAlive;
	}
	final void event_tostring(ByteArrayOutputStream b) {
		this.failProp1 = false;
		int numAlive = 0 ;
		for(int i = 0; i < this.size; i++){
			ByteArrayOutputStream_FlushBeforeRetrieveMonitor monitor = this.elements[i];
			if(!monitor.isTerminated()){
				elements[numAlive] = monitor;
				numAlive++;

				final ByteArrayOutputStream_FlushBeforeRetrieveMonitor monitorfinalMonitor = monitor;
				monitor.Prop_1_event_tostring(b);
				failProp1 |= monitorfinalMonitor.Prop_1_Category_fail;
				if(monitorfinalMonitor.Prop_1_Category_fail) {
					monitorfinalMonitor.Prop_1_handler_fail();
				}
			}
		}
		for(int i = numAlive; i < this.size; i++){
			this.elements[i] = null;
		}
		size = numAlive;
	}
}

class ByteArrayOutputStream_FlushBeforeRetrieveMonitor extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractSynchronizedMonitor implements Cloneable, com.runtimeverification.rvmonitor.java.rt.RVMObject {
	protected Object clone() {
		try {
			ByteArrayOutputStream_FlushBeforeRetrieveMonitor ret = (ByteArrayOutputStream_FlushBeforeRetrieveMonitor) super.clone();
			return ret;
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	WeakReference Ref_b = null;
	WeakReference Ref_o = null;
	int Prop_1_state;
	static final int Prop_1_transition_outputstreaminit[] = {2, 4, 4, 4, 4};;
	static final int Prop_1_transition_write[] = {4, 2, 2, 4, 4};;
	static final int Prop_1_transition_flush[] = {4, 1, 1, 4, 4};;
	static final int Prop_1_transition_close[] = {4, 3, 3, 4, 4};;
	static final int Prop_1_transition_tobytearray[] = {4, 1, 4, 3, 4};;
	static final int Prop_1_transition_tostring[] = {4, 1, 4, 3, 4};;

	boolean Prop_1_Category_fail = false;

	ByteArrayOutputStream_FlushBeforeRetrieveMonitor() {
		Prop_1_state = 0;

	}

	@Override
	public final int getState() {
		return Prop_1_state;
	}

	final boolean Prop_1_event_outputstreaminit(ByteArrayOutputStream b, OutputStream o) {
		{
		}
		if(Ref_b == null){
			Ref_b = new WeakReference(b);
		}
		if(Ref_o == null){
			Ref_o = new WeakReference(o);
		}
		RVM_lastevent = 0;

		Prop_1_state = Prop_1_transition_outputstreaminit[Prop_1_state];
		Prop_1_Category_fail = Prop_1_state == 4;
		return true;
	}

	final boolean Prop_1_event_write(OutputStream o) {
		ByteArrayOutputStream b = null;
		if(Ref_b != null){
			b = (ByteArrayOutputStream)Ref_b.get();
		}
		{
		}
		if(Ref_o == null){
			Ref_o = new WeakReference(o);
		}
		RVM_lastevent = 1;

		Prop_1_state = Prop_1_transition_write[Prop_1_state];
		Prop_1_Category_fail = Prop_1_state == 4;
		return true;
	}

	final boolean Prop_1_event_flush(OutputStream o) {
		ByteArrayOutputStream b = null;
		if(Ref_b != null){
			b = (ByteArrayOutputStream)Ref_b.get();
		}
		{
		}
		if(Ref_o == null){
			Ref_o = new WeakReference(o);
		}
		RVM_lastevent = 2;

		Prop_1_state = Prop_1_transition_flush[Prop_1_state];
		Prop_1_Category_fail = Prop_1_state == 4;
		return true;
	}

	final boolean Prop_1_event_close(OutputStream o) {
		ByteArrayOutputStream b = null;
		if(Ref_b != null){
			b = (ByteArrayOutputStream)Ref_b.get();
		}
		{
		}
		if(Ref_o == null){
			Ref_o = new WeakReference(o);
		}
		RVM_lastevent = 3;

		Prop_1_state = Prop_1_transition_close[Prop_1_state];
		Prop_1_Category_fail = Prop_1_state == 4;
		return true;
	}

	final boolean Prop_1_event_tobytearray(ByteArrayOutputStream b) {
		OutputStream o = null;
		if(Ref_o != null){
			o = (OutputStream)Ref_o.get();
		}
		{
		}
		if(Ref_b == null){
			Ref_b = new WeakReference(b);
		}
		RVM_lastevent = 4;

		Prop_1_state = Prop_1_transition_tobytearray[Prop_1_state];
		Prop_1_Category_fail = Prop_1_state == 4;
		return true;
	}

	final boolean Prop_1_event_tostring(ByteArrayOutputStream b) {
		OutputStream o = null;
		if(Ref_o != null){
			o = (OutputStream)Ref_o.get();
		}
		{
		}
		if(Ref_b == null){
			Ref_b = new WeakReference(b);
		}
		RVM_lastevent = 5;

		Prop_1_state = Prop_1_transition_tostring[Prop_1_state];
		Prop_1_Category_fail = Prop_1_state == 4;
		return true;
	}

	final void Prop_1_handler_fail (){
		{
			RVMLogging.out.println(Level.CRITICAL, "Specification ByteArrayOutputStream_FlushBeforeRetrieve has been violated on line " + com.runtimeverification.rvmonitor.java.rt.ViolationRecorder.getLineOfCode() + ". Documentation for this property can be found at http://runtimeverification.com/monitor/annotated-java/__properties/html/mop/ByteArrayOutputStream_FlushBeforeRetrieve.html");
			RVMLogging.out.println(Level.CRITICAL, "flush() or close() should be invoked before toByteArray() or toString() to get the complete contents." + com.runtimeverification.rvmonitor.java.rt.ViolationRecorder.getLineOfCode());
		}

	}

	final void reset() {
		RVM_lastevent = -1;
		Prop_1_state = 0;
		Prop_1_Category_fail = false;
	}

	// RVMRef_b was suppressed to reduce memory overhead
	// RVMRef_o was suppressed to reduce memory overhead

	//alive_parameters_0 = [ByteArrayOutputStream b]
	boolean alive_parameters_0 = true;
	//alive_parameters_1 = [OutputStream o]
	boolean alive_parameters_1 = true;

	@Override
	protected final void terminateInternal(int idnum) {
		switch(idnum){
			case 0:
			alive_parameters_0 = false;
			break;
			case 1:
			alive_parameters_1 = false;
			break;
		}
		switch(RVM_lastevent) {
			case -1:
			return;
			case 0:
			//outputstreaminit
			//alive_b || alive_o
			if(!(alive_parameters_0 || alive_parameters_1)){
				RVM_terminated = true;
				return;
			}
			break;

			case 1:
			//write
			//alive_b || alive_o
			if(!(alive_parameters_0 || alive_parameters_1)){
				RVM_terminated = true;
				return;
			}
			break;

			case 2:
			//flush
			//alive_b || alive_o
			if(!(alive_parameters_0 || alive_parameters_1)){
				RVM_terminated = true;
				return;
			}
			break;

			case 3:
			//close
			//alive_b || alive_o
			if(!(alive_parameters_0 || alive_parameters_1)){
				RVM_terminated = true;
				return;
			}
			break;

			case 4:
			//tobytearray
			//alive_b || alive_o
			if(!(alive_parameters_0 || alive_parameters_1)){
				RVM_terminated = true;
				return;
			}
			break;

			case 5:
			//tostring
			//alive_b || alive_o
			if(!(alive_parameters_0 || alive_parameters_1)){
				RVM_terminated = true;
				return;
			}
			break;

		}
		return;
	}

	public static int getNumberOfEvents() {
		return 6;
	}

	public static int getNumberOfStates() {
		return 5;
	}

}

public final class ByteArrayOutputStream_FlushBeforeRetrieveRuntimeMonitor implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	private static boolean failProp1 = false;
	private static com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager ByteArrayOutputStream_FlushBeforeRetrieveMapManager;
	static {
		ByteArrayOutputStream_FlushBeforeRetrieveMapManager = new com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager();
		ByteArrayOutputStream_FlushBeforeRetrieveMapManager.start();
	}

	// Declarations for the Lock
	static final ReentrantLock ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock = new ReentrantLock();
	static final Condition ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock_cond = ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock.newCondition();

	private static boolean ByteArrayOutputStream_FlushBeforeRetrieve_activated = false;

	// Declarations for Indexing Trees
	private static Object ByteArrayOutputStream_FlushBeforeRetrieve_b_Map_cachekey_b;
	private static Tuple2<MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor>, ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set> ByteArrayOutputStream_FlushBeforeRetrieve_b_Map_cachevalue;
	private static Object ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map_cachekey_b;
	private static Object ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map_cachekey_o;
	private static ByteArrayOutputStream_FlushBeforeRetrieveMonitor ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map_cachevalue;
	private static Object ByteArrayOutputStream_FlushBeforeRetrieve_o_Map_cachekey_o;
	private static ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set ByteArrayOutputStream_FlushBeforeRetrieve_o_Map_cachevalue;
	private static final MapOfMapSet<MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor>, ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set> ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map = new MapOfMapSet<MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor>, ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set>(0) ;
	private static final MapOfSet<ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set> ByteArrayOutputStream_FlushBeforeRetrieve_o_Map = new MapOfSet<ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set>(1) ;

	public static int cleanUp() {
		int collected = 0;
		// indexing trees
		collected += ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map.cleanUpUnnecessaryMappings();
		collected += ByteArrayOutputStream_FlushBeforeRetrieve_o_Map.cleanUpUnnecessaryMappings();
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

	public static final void outputstreaminitEvent(ByteArrayOutputStream b, OutputStream o) {
		ByteArrayOutputStream_FlushBeforeRetrieve_activated = true;
		while (!ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock.tryLock()) {
			Thread.yield();
		}

		CachedWeakReference wr_b = null;
		CachedWeakReference wr_o = null;
		MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor> matchedLastMap = null;
		ByteArrayOutputStream_FlushBeforeRetrieveMonitor matchedEntry = null;
		boolean cachehit = false;
		if (((b == ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map_cachekey_b) && (o == ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map_cachekey_o) ) ) {
			matchedEntry = ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map_cachevalue;
			cachehit = true;
		}
		else {
			wr_b = new CachedWeakReference(b) ;
			wr_o = new CachedWeakReference(o) ;
			{
				// FindOrCreateEntry
				Tuple2<MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor>, ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set> node_b = ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map.getNodeEquivalent(wr_b) ;
				if ((node_b == null) ) {
					node_b = new Tuple2<MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor>, ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set>() ;
					ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map.putNode(wr_b, node_b) ;
					node_b.setValue1(new MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor>(1) ) ;
					node_b.setValue2(new ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set() ) ;
				}
				MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor> itmdMap = node_b.getValue1() ;
				matchedLastMap = itmdMap;
				ByteArrayOutputStream_FlushBeforeRetrieveMonitor node_b_o = node_b.getValue1() .getNodeEquivalent(wr_o) ;
				matchedEntry = node_b_o;
			}
		}
		// D(X) main:1
		if ((matchedEntry == null) ) {
			if ((wr_b == null) ) {
				wr_b = new CachedWeakReference(b) ;
			}
			if ((wr_o == null) ) {
				wr_o = new CachedWeakReference(o) ;
			}
			// D(X) main:4
			ByteArrayOutputStream_FlushBeforeRetrieveMonitor created = new ByteArrayOutputStream_FlushBeforeRetrieveMonitor() ;
			matchedEntry = created;
			matchedLastMap.putNode(wr_o, created) ;
			// D(X) defineNew:5 for <b>
			{
				// InsertMonitor
				Tuple2<MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor>, ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set> node_b = ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map.getNodeEquivalent(wr_b) ;
				if ((node_b == null) ) {
					node_b = new Tuple2<MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor>, ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set>() ;
					ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map.putNode(wr_b, node_b) ;
					node_b.setValue1(new MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor>(1) ) ;
					node_b.setValue2(new ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set() ) ;
				}
				ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set targetSet = node_b.getValue2() ;
				targetSet.add(created) ;
			}
			// D(X) defineNew:5 for <o>
			{
				// InsertMonitor
				ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set node_o = ByteArrayOutputStream_FlushBeforeRetrieve_o_Map.getNodeEquivalent(wr_o) ;
				if ((node_o == null) ) {
					node_o = new ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set() ;
					ByteArrayOutputStream_FlushBeforeRetrieve_o_Map.putNode(wr_o, node_o) ;
				}
				node_o.add(created) ;
			}
		}
		// D(X) main:8--9
		final ByteArrayOutputStream_FlushBeforeRetrieveMonitor matchedEntryfinalMonitor = matchedEntry;
		matchedEntry.Prop_1_event_outputstreaminit(b, o);
		failProp1 |= matchedEntryfinalMonitor.Prop_1_Category_fail;
		if(matchedEntryfinalMonitor.Prop_1_Category_fail) {
			matchedEntryfinalMonitor.Prop_1_handler_fail();
		}

		if ((cachehit == false) ) {
			ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map_cachekey_b = b;
			ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map_cachekey_o = o;
			ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map_cachevalue = matchedEntry;
		}

		ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock.unlock();
	}

	public static final void writeEvent(OutputStream o) {
		while (!ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock.tryLock()) {
			Thread.yield();
		}

		if (ByteArrayOutputStream_FlushBeforeRetrieve_activated) {
			ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set matchedEntry = null;
			boolean cachehit = false;
			if ((o == ByteArrayOutputStream_FlushBeforeRetrieve_o_Map_cachekey_o) ) {
				matchedEntry = ByteArrayOutputStream_FlushBeforeRetrieve_o_Map_cachevalue;
				cachehit = true;
			}
			else {
				// FindEntry
				ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set node_o = ByteArrayOutputStream_FlushBeforeRetrieve_o_Map.getNodeWithStrongRef(o) ;
				if ((node_o != null) ) {
					matchedEntry = node_o;
				}
			}
			// D(X) main:8--9
			if ((matchedEntry != null) ) {
				matchedEntry.event_write(o);
				failProp1 = matchedEntry.failProp1;

				if ((cachehit == false) ) {
					ByteArrayOutputStream_FlushBeforeRetrieve_o_Map_cachekey_o = o;
					ByteArrayOutputStream_FlushBeforeRetrieve_o_Map_cachevalue = matchedEntry;
				}
			}

		}

		ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock.unlock();
	}

	public static final void flushEvent(OutputStream o) {
		while (!ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock.tryLock()) {
			Thread.yield();
		}

		if (ByteArrayOutputStream_FlushBeforeRetrieve_activated) {
			ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set matchedEntry = null;
			boolean cachehit = false;
			if ((o == ByteArrayOutputStream_FlushBeforeRetrieve_o_Map_cachekey_o) ) {
				matchedEntry = ByteArrayOutputStream_FlushBeforeRetrieve_o_Map_cachevalue;
				cachehit = true;
			}
			else {
				// FindEntry
				ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set node_o = ByteArrayOutputStream_FlushBeforeRetrieve_o_Map.getNodeWithStrongRef(o) ;
				if ((node_o != null) ) {
					matchedEntry = node_o;
				}
			}
			// D(X) main:8--9
			if ((matchedEntry != null) ) {
				matchedEntry.event_flush(o);
				failProp1 = matchedEntry.failProp1;

				if ((cachehit == false) ) {
					ByteArrayOutputStream_FlushBeforeRetrieve_o_Map_cachekey_o = o;
					ByteArrayOutputStream_FlushBeforeRetrieve_o_Map_cachevalue = matchedEntry;
				}
			}

		}

		ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock.unlock();
	}

	public static final void closeEvent(OutputStream o) {
		while (!ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock.tryLock()) {
			Thread.yield();
		}

		if (ByteArrayOutputStream_FlushBeforeRetrieve_activated) {
			ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set matchedEntry = null;
			boolean cachehit = false;
			if ((o == ByteArrayOutputStream_FlushBeforeRetrieve_o_Map_cachekey_o) ) {
				matchedEntry = ByteArrayOutputStream_FlushBeforeRetrieve_o_Map_cachevalue;
				cachehit = true;
			}
			else {
				// FindEntry
				ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set node_o = ByteArrayOutputStream_FlushBeforeRetrieve_o_Map.getNodeWithStrongRef(o) ;
				if ((node_o != null) ) {
					matchedEntry = node_o;
				}
			}
			// D(X) main:8--9
			if ((matchedEntry != null) ) {
				matchedEntry.event_close(o);
				failProp1 = matchedEntry.failProp1;

				if ((cachehit == false) ) {
					ByteArrayOutputStream_FlushBeforeRetrieve_o_Map_cachekey_o = o;
					ByteArrayOutputStream_FlushBeforeRetrieve_o_Map_cachevalue = matchedEntry;
				}
			}

		}

		ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock.unlock();
	}

	public static final void tobytearrayEvent(ByteArrayOutputStream b) {
		while (!ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock.tryLock()) {
			Thread.yield();
		}

		if (ByteArrayOutputStream_FlushBeforeRetrieve_activated) {
			Tuple2<MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor>, ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set> matchedEntry = null;
			boolean cachehit = false;
			if ((b == ByteArrayOutputStream_FlushBeforeRetrieve_b_Map_cachekey_b) ) {
				matchedEntry = ByteArrayOutputStream_FlushBeforeRetrieve_b_Map_cachevalue;
				cachehit = true;
			}
			else {
				// FindEntry
				Tuple2<MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor>, ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set> node_b = ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map.getNodeWithStrongRef(b) ;
				if ((node_b != null) ) {
					matchedEntry = node_b;
				}
			}
			// D(X) main:8--9
			if ((matchedEntry != null) ) {
				ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set stateTransitionedSet = matchedEntry.getValue2() ;
				if ((stateTransitionedSet != null) ) {
					stateTransitionedSet.event_tobytearray(b);
					failProp1 = stateTransitionedSet.failProp1;

					if ((cachehit == false) ) {
						ByteArrayOutputStream_FlushBeforeRetrieve_b_Map_cachekey_b = b;
						ByteArrayOutputStream_FlushBeforeRetrieve_b_Map_cachevalue = matchedEntry;
					}
				}
			}

		}

		ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock.unlock();
	}

	public static final void tostringEvent(ByteArrayOutputStream b) {
		while (!ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock.tryLock()) {
			Thread.yield();
		}

		if (ByteArrayOutputStream_FlushBeforeRetrieve_activated) {
			Tuple2<MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor>, ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set> matchedEntry = null;
			boolean cachehit = false;
			if ((b == ByteArrayOutputStream_FlushBeforeRetrieve_b_Map_cachekey_b) ) {
				matchedEntry = ByteArrayOutputStream_FlushBeforeRetrieve_b_Map_cachevalue;
				cachehit = true;
			}
			else {
				// FindEntry
				Tuple2<MapOfMonitor<ByteArrayOutputStream_FlushBeforeRetrieveMonitor>, ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set> node_b = ByteArrayOutputStream_FlushBeforeRetrieve_b_o_Map.getNodeWithStrongRef(b) ;
				if ((node_b != null) ) {
					matchedEntry = node_b;
				}
			}
			// D(X) main:8--9
			if ((matchedEntry != null) ) {
				ByteArrayOutputStream_FlushBeforeRetrieveMonitor_Set stateTransitionedSet = matchedEntry.getValue2() ;
				if ((stateTransitionedSet != null) ) {
					stateTransitionedSet.event_tostring(b);
					failProp1 = stateTransitionedSet.failProp1;

					if ((cachehit == false) ) {
						ByteArrayOutputStream_FlushBeforeRetrieve_b_Map_cachekey_b = b;
						ByteArrayOutputStream_FlushBeforeRetrieve_b_Map_cachevalue = matchedEntry;
					}
				}
			}

		}

		ByteArrayOutputStream_FlushBeforeRetrieve_RVMLock.unlock();
	}

}
