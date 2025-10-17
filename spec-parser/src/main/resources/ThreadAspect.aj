package mop;
import org.aspectj.lang.*;
import java.util.*;
import java.util.concurrent.*;

public aspect ThreadAspect {
    pointcut threadStart() : (
        call(* Thread+.start()) ||
        call(* ExecutorService+.execute(..)) ||
        call(* ExecutorService+.submit(..)) ||
        call(* ForkJoinPool+.submit(..)) ||
        call(* ForkJoinPool+.execute(..)) ||
        call(* ThreadPoolExecutor+.execute(..)) ||
        call(* ThreadPoolExecutor+.submit(..)) ||
        call(* CompletableFuture+.runAsync(..)) ||
        call(* CompletableFuture+.supplyAsync(..)) ||
        call(* Timer+.schedule(..))
        ) && !adviceexecution() && BaseAspect.notwithin();
    before() : threadStart() {
        if (!edu.lazymop.tinymop.monitoring.GlobalMonitorManager.isMultiThreaded) {
            System.out.println("[TinyMOP] Multiple threads detected.");
            edu.lazymop.tinymop.monitoring.GlobalMonitorManager.isMultiThreaded = true;
        }
    }
}
