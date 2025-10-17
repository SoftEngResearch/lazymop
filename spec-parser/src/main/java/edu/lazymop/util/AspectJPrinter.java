package edu.lazymop.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level  ;

import javamop.JavaMOPMain;
import javamop.output.AspectJCode;
import javamop.output.combinedaspect.CombinedAspect;
import javamop.output.combinedaspect.event.EndObject;
import javamop.output.combinedaspect.event.EndThread;
import javamop.output.combinedaspect.event.EventManager;
import javamop.output.combinedaspect.event.StartThread;
import javamop.output.combinedaspect.event.advice.AdviceAndPointCut;
import javamop.output.combinedaspect.event.advice.AdviceBody;
import javamop.parser.ast.mopspec.EventDefinition;
import javamop.parser.ast.mopspec.MOPParameter;
import org.aspectj.lang.JoinPoint;

public class AspectJPrinter {

    private static final Logger LOGGER = Logger.getGlobal();

    public static String printAspectJCode(AspectJCode ajCode) {
        return printWholeAspect(ajCode);
    }

    private static String printWholeAspect(AspectJCode ajCode) {
        String ret = "";
        ret += ajCode.getPackageDecl();
        ret += "\n";
        ret += ajCode.getImports().toString().replaceAll("import javamoprt.*", "");

        ret += "\n";

        ret += "import org.aspectj.lang.JoinPoint;\n"
               + "import org.aspectj.lang.reflect.SourceLocation;\n"
               + "import edu.lazymop.tinymop.monitoring.*;\n"
               + "import edu.lazymop.tinymop.monitoring.monitorsmanager.*;";

        ret += "\n";


        // The order of these two is really important.
        if (ajCode.getSystemAspect() != null) {
            ret += "aspect " + ajCode.getName() + "OrderAspect {\n";
            ret += "declare precedence : ";
            ret += ajCode.getSystemAspect().getSystemAspectName() + "";
            ret += ", ";
            ret += ajCode.getSystemAspect().getSystemAspectName() + "2";
            ret += ", ";
            ret += ajCode.getAspect().getAspectName();
            ret += ";\n";

            ret += "}\n";
            ret += "\n";
        }

        ret += printAspect(ajCode.getAspect());

        if (ajCode.getSystemAspect() != null) {
            ret += "\n" + ajCode.getSystemAspect();
        }

        return ret;
    }

    /**
     * Code to use the combined aspect with the backing of RV-Montior.
     *
     * @return The generated code.
     */
    public static String printAspect(CombinedAspect aspect) {
        String ret = "";
        ret += aspect.statManager.statClass();
        String aspectName = aspect.getName().replace("MonitorAspect", "");
        String specName = aspectName.replace("_", "");
        ret += "public aspect " + specName + " {\n";

        // Field
        ret += "private boolean[] visited = new boolean[100000];\n\n";

        // Constructor
        ret += "public " + specName + "() {\n";

        ret += aspect.eventManager.printConstructor();

        ret += "}\n";
        ret += "\n";

        ret += "public int getLocation(JoinPoint.StaticPart jp, JoinPoint.StaticPart jp2) {\n"
                + "\t\tint id = jp.getId();\n"
                + "\t\tif (visited[id]) {\n"
                + "\t\t\treturn id;\n"
                + "\t\t}\n"
                + "\t\tvisited[id] = true;\n"
                + "\t\t\n"
                + "\t\tSourceLocation loc = jp.getSourceLocation();\n"
                + "\t\tSourceLocation loc2 = jp2.getSourceLocation();\n"
                + "\t\tif (loc == null) {\n"
                + "\t\t\t" + specName + "MonitorManager.getManagerInstance().notifyMapping(id, \"UNKNOWN\", false);\n"
                + "\t\t\treturn id;\n"
                + "\t\t}\n"
                + "\t\tString klass = loc.getWithinType().getName();\n"
                + "\t\tString location = klass + \".\" + jp2.getSignature().getName()"
                + "+ \":\" + loc.getFileName() + \":\" + loc.getLine() + \":\" + loc2.getLine();\n"
                + "boolean fromChangedClass = GlobalMonitorManager.isChangedMethods(klass);\n"
                + "\t\t" + specName + "MonitorManager.getManagerInstance().notifyMapping(id, location, fromChangedClass);\n"
                + "\t\treturn id;\n"
                + "\t}\n\n";

        ret += aspect.statManager.advice();

        ret += aspect.lockManager.decl();

        ret += printAdvices(aspect.eventManager, aspect);


        ret += "}\n";
        return ret;
    }

    public static String printAdvices(EventManager eventManager, CombinedAspect aspect) {
        String specName = aspect.getFileName();
        String cleanSpecName = specName.replace("_", "");

        String ret = "";

        ret += "pointcut " + eventManager.getCommonPointcut() + "() : ";

        ret += "!within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) "
                + "&& !adviceexecution() && " + cleanSpecName + "BaseAspect.notwithin();\n";

        int numAdvice = 1;
        ArrayList<AdviceAndPointCut> advices = eventManager.adjustAdviceOrder();
        for (AdviceAndPointCut advice : advices) {
            advice.setPointCutPrinted(false);
        }

        for (AdviceAndPointCut advice : advices) {
            if (JavaMOPMain.empty_advicebody) {
                ret += "// " + numAdvice++ + "\n";
            }
            ret += printAdvice(advice);

            ret += "\n";
            if (advice.beCounted) {
                ret += "\n";
                ret += "// Declaration of the count variable for above pointcut\n";
                ret += "static int " + advice.getPointCutName() + "_count = 0;";
                ret += "\n\n\n";
            }
        }

        for (EndObject endObject : eventManager.getEndObjectEvents()) {
            ret += endObject.printDecl();
            ret += "\n";
        }

        for (EndThread endThread : eventManager.getEndThreadEvents()) {
            // Replace RuntimeMonitor with MonitorManager, add getLocation(thisJoinPointStaticPart), false to argument
            for (String line : endThread.printAdvices().split("\n")) {
                if (line.contains(specName + "RuntimeMonitor")) {
                    boolean addComma = !line.contains("(thisJoinPointStaticPart, thisEnclosingJoinPointStaticPart)");
                    ret += line.replace(specName + "RuntimeMonitor." + specName + "_",
                                    cleanSpecName + "MonitorManager.getManagerInstance().")
                            .replace("thisJoinPointStaticPart, thisEnclosingJoinPointStaticPart, ", "")
                            .replace(");", (addComma ? ", " : "")
                                    + "getLocation(thisJoinPointStaticPart, thisEnclosingJoinPointStaticPart), false);")
                            + "\n";
                } else {
                    ret += line + "\n";
                }
            }
            ret += "\n";
        }

        for (StartThread startThread : eventManager.getStartThreadEvents()) {
            // Replace RuntimeMonitor with MonitorManager, add getLocation(thisJoinPointStaticPart), false to argument
            for (String line : startThread.printAdvices().split("\n")) {
                if (line.contains(specName + "RuntimeMonitor")) {
                    boolean addComma = !line.contains("(thisJoinPointStaticPart, thisEnclosingJoinPointStaticPart)");
                    ret += line.replace(specName + "RuntimeMonitor." + specName + "_",
                            cleanSpecName + "MonitorManager.getManagerInstance().")
                            .replace("thisJoinPointStaticPart, thisEnclosingJoinPointStaticPart, ", "")
                            .replace(");", (addComma ? ", " : "")
                                    + "getLocation(thisJoinPointStaticPart, thisEnclosingJoinPointStaticPart), false);")
                            + "\n";
                } else {
                    ret += line + "\n";
                }
            }
            ret += "\n";
        }
        ret += eventManager.getEndProgramEvent().printHookThread();

        return ret;
    }

    public static String printAdvice(AdviceAndPointCut advice) {
        String ret = "";

        String methodName = "foo";

        String pointcutStr = advice.getPointCut().toRVString();
        if (!advice.isPointCutPrinted()) {
            ret += "pointcut " + advice.getPointCutName();
            ret += "(";
            ret += advice.getParametersDeclStr();
            ret += ")";
            ret += " : ";
            if (pointcutStr != null && pointcutStr.length() != 0) {
                ret += "(";
                ret += pointcutStr;
                ret += ")";
                ret += " && ";
            }
            ret += advice.getCommonPointcut() + "();\n";
            advice.setPointCutPrinted();
        }

        if (advice.isAround) {
            ret += advice.retType + " ";
        }

        ret += advice.pos + " (" + advice.getParametersDeclStr() + ") ";

        if (advice.retVal != null && advice.retVal.size() > 0) {
            ret += "returning (";
            ret += advice.retVal.parameterDeclString();
            ret += ") ";
        }

        if (advice.throwVal != null && advice.throwVal.size() > 0) {
            ret += "throwing (";
            ret += advice.throwVal.parameterDeclString();
            ret += ") ";
        }

        ret += ": " + advice.getPointCutName() + "(" + advice.getParameters().parameterString() + ") {\n";

        if (advice.getAroundLocalDecl() != null) {
            ret += advice.getAroundLocalDecl();
        }


        // Call method here MOPNameRuntimeMonitor.nameEvent()
        // If there's thread var, replace with t (currentThread),
        // and also generate Thread t = currentThread before it
        // If there's return/ throw pointcut, cat in the end

        for (MOPParameter threadVar : advice.getThreadVars()) {
            ret += "Thread " + threadVar.getName() + " = Thread.currentThread();\n";
        }

        Iterator<EventDefinition> iter;
        if (advice.pos.equals("before")) {
            iter = advice.getEvents().descendingIterator();
        } else {
            iter = advice.getEvents().iterator();
        }

        while (iter.hasNext()) {
            EventDefinition event = iter.next();

            AdviceBody innerAdvice = advice.getAdvices().get(event);

            if (advice.getAdvices().size() > 1) {
                ret += "//" + innerAdvice.getMOPSpec().getName() + "_"
                        + event.getUniqueId() + "\n";
            }

            String countCond = event.getCountCond();

            if (countCond != null && countCond.length() != 0) {
                ret += "++" + advice.getPointCutName() + "_count;\n";
                countCond = countCond.replaceAll("count", advice.getPointCutName() + "_count");
                ret += "if (" + countCond + ") {\n";
            }

            methodName = getEventName(innerAdvice.getMOPSpec().getName().replace("_", ""), event.getId());
            ret += methodName;
            ret += "(";

            // Parameters
            // Original (including threadVar)
            String original = event.getParameters().parameterString();
            ret += original;

            boolean hasArgument = !original.isEmpty();

            // Parameters in returning pointcut
            if (event.getRetVal() != null && event.getRetVal().size() > 0) {
                String retParameters = event.getRetVal().parameterString();
                if (retParameters.length() > 0) {
                    hasArgument = true;

                    if (original == null || original.length() == 0) {
                        ret += retParameters;
                    } else {
                        ret += ", " + retParameters;
                    }
                }
            }

            // Parameters in throwing pointcut
            if (event.getThrowVal() != null && event.getThrowVal().size() > 0) {
                String throwParameters = event.getThrowVal().parameterString();
                if (throwParameters.length() > 0) {
                    hasArgument = true;

                    if (original == null || original.length() == 0) {
                        ret += throwParameters;
                    } else {
                        ret += ", " + throwParameters;
                    }
                }
            }

            // __STATICSIG should be passed as an argument because rv-monitor cannot infer
            if (event.has__STATICSIG()) {
                hasArgument = true;

                String staticsig = "thisJoinPoint.getStaticPart().getSignature()";
                if (original == null || original.length() == 0) {
                    ret += staticsig;
                } else {
                    ret += ", " + staticsig;
                }
            }

            if (hasArgument) {
                ret += ", ";
            }

            ret += "getLocation(thisJoinPointStaticPart, thisEnclosingJoinPointStaticPart), " + event.isStartEvent();

            ret += ");\n";

            if (countCond != null && countCond.length() != 0) {
                ret += "}\n";
            }
        }

        if (advice.getAroundAdviceReturn() != null) {
            ret += advice.getAroundAdviceReturn();
        }
        // CHECKSTYLE:OFF
        // ret += "System.out.println(\"Event: " + methodName + "\");\n";
        // CHECKSTYLE:ON
        ret += "}\n";

        return ret;
    }

    private static String getEventName(String specName, String eventId) {
        StringBuilder name = new StringBuilder();
        name.append(specName);
        name.append("MonitorManager.getManagerInstance().");
        name.append(eventId);
        name.append("Event");
        return name.toString();
    }
}
