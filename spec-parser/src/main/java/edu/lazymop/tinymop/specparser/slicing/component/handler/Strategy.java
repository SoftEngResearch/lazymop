//  Based on JavaMOP's com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.event.itf.EventMethodBody

package edu.lazymop.tinymop.specparser.slicing.component.handler;

import java.util.HashSet;

import com.runtimeverification.rvmonitor.java.rvj.output.NotImplementedException;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameter;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameters;

public class Strategy {
    protected final boolean needsWeakReferenceLookup;
    protected final boolean shouldCreateIndexingTreeIntemediateNodes;
    protected final boolean needsTimeTracking;
    protected final boolean mayCreateMonitors;
    protected final boolean mayCopyFromOtherMonitors;
    protected final boolean needsNullCheckForTransition;

    public Strategy(boolean isCreationEvent, boolean isGeneral,
             boolean fullybound, RVMParameters eventParams,
             HashSet<RVMParameter> disableParams) {
        // In general, weak references should be retrieved according to the
        // strong references
        // carried by the event. In the special case, however, such lookup
        // is unnecessary
        // considering that weak references are needed only for inserting
        // nodes into indexing trees.
        this.needsWeakReferenceLookup = isGeneral || isCreationEvent;

        // In general, all the intermediate nodes should be created while
        // looking up
        // the entry corresponding to the parameters that the event carries.
        // However,
        // for the special case, only creation events need such behavior,
        // because
        // non-creation events would not create any monitors and, therefore,
        // the
        // intermediate nodes are unnecessary.
        this.shouldCreateIndexingTreeIntemediateNodes = isGeneral
                || isCreationEvent;

        // This field tells whether or not keeping track of the 'disable'
        // field is needed.

        // The following is similar to the 'doDisable' field in JavaMOP
        // 3.0,
        // but it keeps the 'disable' field regardless of this field.
        // So, I'm
        // not using this logic.
        /*
         * boolean disable = false; for (RVMParameter prm : eventParams)
         * { if (disableParams.contains(prm)) { disable = true; break; }
         * } this.needsTimeTracking = disable;
         */
        this.needsTimeTracking = isGeneral;


        // This field tells whether anything related to monitor creation is
        // ever needed.
        // If this field is false, the routine for lines 2--5 in 'main' can
        // be skipped.
        this.mayCreateMonitors = isGeneral || isCreationEvent;

        // This field tells whether this event may need to copy-construct a
        // monitor,
        // which is defined as the 'defineTo' function in D(X). If this
        // field is false,
        // invoking 'createNewMonitorState' on line 2 in 'main' is
        // unnecessary.
        this.mayCopyFromOtherMonitors = isGeneral;

        // This field tells whether null-check is necessary for state
        // transitions, described
        // on lines 8--9 in 'main'.
        boolean needed = false;
        if (!isCreationEvent) {
            // If the event is not a creation one, then the matched
            // entry can be null.
            // However, if the matched entry is not a set, it would
            // never be null because
            // the generated code always creates a set.
            if (isGeneral) {
                if (fullybound) {
                    needed = true;
                }
            } else {
                needed = true;
            }
        }
        this.needsNullCheckForTransition = needed;

        this.validate();
    }

    private void validate() {
        // The former should be no stricter than the latter. If this is not
        // the case,
        // the latter cannot be reached, which is wrong.
        if (!this.mayCreateMonitors && this.mayCopyFromOtherMonitors) {
            throw new NotImplementedException();
        }

        // At this moment, it is believed that the following two fields are
        // related.
        // Also, copying from other monitors seems impossible without time
        // tracking
        // because of time checking on line 2 in 'defineTo'.
        if (this.needsTimeTracking != this.mayCopyFromOtherMonitors) {
            throw new NotImplementedException();
        }
    }
}