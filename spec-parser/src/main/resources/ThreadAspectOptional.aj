package mop;
import org.aspectj.lang.*;
import java.util.*;
import java.util.concurrent.*;

public aspect ThreadAspect {
    pointcut threadStart() : (
        call(* Thread+.start()) ||
        call(* Executor+.execute(..)) ||
        call(* ExecutorService+.submit(..)) ||
        call(* ExecutorService+.invokeAll(..)) ||
        call(* ExecutorService+.invokeAny(..)) ||
        call(* CompletionService+.submit(..)) ||
        call(* ScheduledExecutorService+.schedule*(..)) ||
        call(* ForkJoinPool+.invoke(..)) ||
        call(* ForkJoinTask+.fork()) ||
        call(* ForkJoinTask+.invokeAll(..)) ||
        call(* CompletionStage+.*Async(..)) ||
        call(* Timer+.schedule*(..))
        ) && !adviceexecution() && BaseAspect.notwithin();
    before() : threadStart() {
        if (!edu.lazymop.tinymop.monitoring.GlobalMonitorManager.isMultiThreaded) {
            System.out.println("[TinyMOP] Multiple threads detected.");
            edu.lazymop.tinymop.monitoring.GlobalMonitorManager.isMultiThreaded = true;
        }
    }
}
