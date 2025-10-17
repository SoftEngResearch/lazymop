package mop;
import org.aspectj.lang.*;
import java.util.*;

// Limitation: Does not support PUT
public aspect TestNameAspect {
    public boolean[] visited = new boolean[100000];
    public static Stack<Integer> tests = new Stack<>();
    public static int locationID = 0;

    pointcut testExec() : execution(@(*..Test || *..Before || *..After) * *(..)) && !adviceexecution() && BaseAspect.notwithin();
    before() : testExec() {
        locationID = thisJoinPointStaticPart.getId() + 1;

        if (visited[locationID]) {
            edu.lazymop.tinymop.monitoring.GlobalMonitorManager.updateRunningTest(locationID);
        } else {
            visited[locationID] = true;

            String name = thisJoinPointStaticPart.getSourceLocation().getWithinType().getName() + "." +
                            thisJoinPointStaticPart.getSignature().getName() + "(" +
                            thisJoinPointStaticPart.getSourceLocation().toString() + ")";

            System.out.println("[TinyMOP] Running new test " + name + " (location ID: " + locationID + ")");
            edu.lazymop.tinymop.monitoring.GlobalMonitorManager.addRunningTest(name, locationID);
        }
        tests.push(locationID);
    }

    after() : testExec() {
        if (locationID > 0) {
            if (!tests.isEmpty()) {
                try {
                    tests.pop();
                    if (!tests.isEmpty()) {
                        locationID = tests.peek();
                    } else {
                        locationID = 0;
                    }
                } catch (EmptyStackException e) {
                    System.err.println("EmptyStackException");
                    locationID = 0;
                }
            } else {
                locationID = 0;
            }
            edu.lazymop.tinymop.monitoring.GlobalMonitorManager.updateRunningTest(locationID);
        }
    }
}
