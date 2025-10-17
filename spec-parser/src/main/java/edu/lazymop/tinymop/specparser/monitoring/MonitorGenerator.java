package edu.lazymop.tinymop.specparser.monitoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.configuration.PrettyPrinterConfiguration;
import edu.lazymop.util.Logger;

public class MonitorGenerator {

    private static final Logger LOGGER = Logger.getGlobal();

    private MonitorGenerationUtil monGenUtil;

    private String dotLocation;

    private String monitorClassName;

    private Map<String, String> eventNameToIDMapping;

    /**
     * Construct a MonitorGenerator object.
     *
     * @param monGenUtil Object that hold all the information that we need to generate automaton code.
     * @param dotLocation Where on the filesystem the dot file that represents the automaton should be stored.
     */
    public MonitorGenerator(MonitorGenerationUtil monGenUtil, String dotLocation) {
        this.monGenUtil = monGenUtil;
        this.dotLocation = dotLocation;
        this.monitorClassName = monGenUtil.getSpecName().replace("_", "") + "Monitor";
        this.eventNameToIDMapping = new HashMap<>();

        int iter = 1;
        for (String eventName : monGenUtil.getEventNames()) {
            eventNameToIDMapping.put(eventName, "E" + iter);
            // monGenUtil.getSpecName() + "," + eventName + "," + iter
            iter += 1;
        }
    }

    /**
     * Invoking this method will generate the automaton code for the that produced this.monitor.
     *
     * @return Generated java code for Monitors that can check traces that are related to the spec.
     */
    public String generateMonitorCode() {
        CompilationUnit code = new CompilationUnit();
        code.setPackageDeclaration("edu.lazymop.tinymop.monitoring.monitors");
        ClassOrInterfaceType interfaceType = new ClassOrInterfaceType("RuntimeMonitor");
        ClassOrInterfaceDeclaration klass = code.addClass(monitorClassName)
                .setExtendedTypes(new NodeList<>(Arrays.asList(interfaceType)));
        addImports(code);
        addEnums(klass);
        addFields(klass);
        addConstructor(klass);
        addAutomatonBuilder(klass);
        addHandler(klass);
        addStringRunner(klass);
        addEventRunner(klass);
        addReset(klass);
        addToDot(klass);
        final String out = new PrettyPrinter(new PrettyPrinterConfiguration()
                .setEndOfLineCharacter("\n")
                .setMaxEnumConstantsToAlignHorizontally(1)
                ).print(code);
        return out;
    }

    private void addToDot(ClassOrInterfaceDeclaration klass) {
        MethodDeclaration method = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PUBLIC),
                new VoidType(), "toDot");
        method.addThrownException(new ClassOrInterfaceType("IOException"));

        VariableDeclarationExpr streamDecl
                = new VariableDeclarationExpr(new ClassOrInterfaceType("FileOutputStream"), "fos");

        Statement assignStream = new ExpressionStmt(new AssignExpr(
                streamDecl,
                new ObjectCreationExpr(null,
                        new ClassOrInterfaceType("FileOutputStream"),
                        new NodeList<>(Arrays.asList(new StringLiteralExpr(this.dotLocation)))),
                AssignExpr.Operator.ASSIGN));

        FieldAccessExpr configField = new FieldAccessExpr(new ThisExpr(), "config");
        MethodCallExpr generateStmt = new MethodCallExpr(configField, "generateDotFileInto");
        generateStmt.addArgument(new NameExpr("fos"));
        generateStmt.addArgument(new BooleanLiteralExpr(true));
        method.setBody(new BlockStmt().addStatement(assignStream).addStatement(generateStmt));
        klass.addMember(method);
    }

    private void addReset(ClassOrInterfaceDeclaration klass) {
        MethodDeclaration method = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PUBLIC),
                new VoidType(),
                "reset");

        BlockStmt body = new BlockStmt();
        method.setBody(body);
        // this.trace = new ArrayList<>();
        AssignExpr setEmptyTrace = new AssignExpr(
                new FieldAccessExpr(new ThisExpr(), "trace"),
                new ObjectCreationExpr().setType(new ClassOrInterfaceType(null, "ArrayList<>")),
                AssignExpr.Operator.ASSIGN
        );
        body.addStatement(setEmptyTrace);

        FieldAccessExpr automatonField = new FieldAccessExpr(new ThisExpr(), "automaton");
        Statement assignAutomaton = new ExpressionStmt(new AssignExpr(
                automatonField, getStateMachineObject(body),
                AssignExpr.Operator.ASSIGN));
        body.addStatement(assignAutomaton);

        // this.verdict = VerdictCategory.DONTKNOW
        AssignExpr setVerdict = new AssignExpr(
                new FieldAccessExpr(new ThisExpr(), "verdict"),
                new FieldAccessExpr(new NameExpr("VerdictCategory"), "DONTKNOW"),
                AssignExpr.Operator.ASSIGN
        );
        body.addStatement(setVerdict);

        klass.addMember(method);
    }

    private void addEventRunner(ClassOrInterfaceDeclaration klass) {
        MethodDeclaration method = new MethodDeclaration(Modifier.createModifierList(Modifier.Keyword.PUBLIC),
                new VoidType(), "runAutomatonOnEvents");
        String trace = "trace";
        method.addParameter("List<Event>", trace);
        FieldAccessExpr traceField = new FieldAccessExpr(new ThisExpr(), trace);
        BlockStmt block = new BlockStmt();

        Statement assignTrace = new ExpressionStmt(new AssignExpr(
                traceField, new NameExpr(trace),
                AssignExpr.Operator.ASSIGN));

        VariableDeclarationExpr eventDecl
                = new VariableDeclarationExpr(new ClassOrInterfaceType("Event"), "event");

        BlockStmt loopBody = new BlockStmt();
        FieldAccessExpr automatonField = new FieldAccessExpr(new ThisExpr(), "automaton");
        MethodCallExpr fireExpr = new MethodCallExpr(automatonField, "fire");
        fireExpr.addArgument(new NameExpr("event"));
        loopBody.addStatement(fireExpr);
        Statement forLoop = new ForEachStmt(eventDecl, new NameExpr(trace), loopBody);

        block.addStatement(assignTrace);
        block.addStatement(forLoop);
        method.setBody(block);
        klass.addMember(method);
    }

    private void addStringRunner(ClassOrInterfaceDeclaration klass) {
        ClassOrInterfaceType returnType = new ClassOrInterfaceType("VerdictCategory");
        MethodDeclaration method = new MethodDeclaration(Modifier.createModifierList(Modifier.Keyword.PUBLIC),
                returnType, "runAutomatonOnStrings");
        String trace = "trace";
        method.addParameter("List<String>", trace);
        FieldAccessExpr traceField = new FieldAccessExpr(new ThisExpr(), trace);
        BlockStmt block = new BlockStmt();

        Statement assignTrace = new ExpressionStmt(new AssignExpr(
                traceField, new ObjectCreationExpr(
                        null,
                              new ClassOrInterfaceType("ArrayList<>"),
                              new NodeList<>()),
                AssignExpr.Operator.ASSIGN));

        VariableDeclarationExpr eventDecl
                = new VariableDeclarationExpr(new ClassOrInterfaceType("String"), "event");

        BlockStmt loopBody = new BlockStmt();

        MethodCallExpr valueOf = new MethodCallExpr(new NameExpr("Event"), "valueOf");
        valueOf.addArgument(new MethodCallExpr(new NameExpr("event"), "toUpperCase"));

        MethodCallExpr add = new MethodCallExpr(traceField, "add");
        add.addArgument(new NameExpr("currentEvent"));

        AssignExpr currentEventAssign = new AssignExpr(
                new VariableDeclarationExpr(new ClassOrInterfaceType("Event"), "currentEvent"),
                valueOf,
                AssignExpr.Operator.ASSIGN);

        FieldAccessExpr automatonField = new FieldAccessExpr(new ThisExpr(), "automaton");
        MethodCallExpr fireExpr = new MethodCallExpr(automatonField, "fire");
        fireExpr.addArgument(new NameExpr("currentEvent"));
        loopBody.addStatement(currentEventAssign);
        loopBody.addStatement(add);
        loopBody.addStatement(fireExpr);
        Statement forLoop = new ForEachStmt(eventDecl, new NameExpr(trace), loopBody);

        block.addStatement(assignTrace);
        block.addStatement(forLoop);

        FieldAccessExpr verdictField = new FieldAccessExpr(new ThisExpr(), "verdict");
        ReturnStmt returnStmt = new ReturnStmt(verdictField);

        block.addStatement(returnStmt);

        method.setBody(block);
        klass.addMember(method);
    }

    private void addHandler(ClassOrInterfaceDeclaration klass) {
        MethodDeclaration method = new MethodDeclaration(
                Modifier.createModifierList(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC),
                new VoidType(), "handler");
        String transition = "transition";
        method.addParameter("Transition<State, Event>", transition);
        BlockStmt block = new BlockStmt();
        NameExpr traceField = new NameExpr("trace");
        NameExpr nameField = new NameExpr("specName");
        NameExpr scope = new NameExpr(transition);
        MethodCallExpr getSource = new MethodCallExpr(scope, "getSource");
        MethodCallExpr getTrigger = new MethodCallExpr(scope, "getTrigger");
        MethodCallExpr getTransition = new MethodCallExpr(scope, "getDestination");

        BinaryExpr out = new BinaryExpr(new StringLiteralExpr("Trace: "), traceField, BinaryExpr.Operator.PLUS);
        out = new BinaryExpr(out, new StringLiteralExpr(" violated specification "), BinaryExpr.Operator.PLUS);
        out = new BinaryExpr(out, nameField, BinaryExpr.Operator.PLUS);
        out = new BinaryExpr(out, new StringLiteralExpr(" on transition "), BinaryExpr.Operator.PLUS);
        out = new BinaryExpr(out, getSource, BinaryExpr.Operator.PLUS);
        out = new BinaryExpr(out, new StringLiteralExpr(" - "), BinaryExpr.Operator.PLUS);
        out = new BinaryExpr(out, getTrigger, BinaryExpr.Operator.PLUS);
        out = new BinaryExpr(out, new StringLiteralExpr(" -> "), BinaryExpr.Operator.PLUS);
        out = new BinaryExpr(out, getTransition, BinaryExpr.Operator.PLUS);

        //CHECKSTYLE:OFF
        MethodCallExpr sout = new MethodCallExpr("System.out.println", out);
        //CHECKSTYLE:ON

        AssignExpr verdictAssign = new AssignExpr(
                new NameExpr("verdict"),
                new NameExpr("VerdictCategory.VIOLATING"),
                AssignExpr.Operator.ASSIGN);

        // Don't add print statement to stdout
//        block.addStatement(sout);
        block.addStatement(verdictAssign);
        method.setBody(block);
        klass.addMember(method);
    }

    private void addAutomatonBuilder(ClassOrInterfaceDeclaration klass) {
        ClassOrInterfaceType returnType = new ClassOrInterfaceType("StateMachine<State, Event>");
        MethodDeclaration method = new MethodDeclaration(Modifier.createModifierList(Modifier.Keyword.PRIVATE),
                returnType, "buildAutomaton");
        FieldAccessExpr configField = new FieldAccessExpr(new ThisExpr(), "config");
        BlockStmt block = new BlockStmt();

        Statement assignConfig = new ExpressionStmt(new AssignExpr(
                configField, new ObjectCreationExpr(null,
                new ClassOrInterfaceType("StateMachineConfig<>"), new NodeList<>()),
                AssignExpr.Operator.ASSIGN));

        block.addStatement(assignConfig);
        for (String state : monGenUtil.getTransitions().keySet()) {
            MethodCallExpr configure = new MethodCallExpr(configField, "configure");
            configure.addArgument("State." + state.toUpperCase());
            Expression top = configure;
            if ((monGenUtil.getCategory().equals("violation") && state.equals(monGenUtil.getCategory()))
                || monGenUtil.getCategory().equals("match") && isAliasedToCategory(state, monGenUtil.getCategory())
                || monGenUtil.getCategory().equals("err") && state.equals(monGenUtil.getCategory())
                || monGenUtil.getCategory().equals("unsafe") && state.equals(monGenUtil.getCategory())) {
                // this is the "violating state"
                Collection<String> transitions = monGenUtil.getTransitions().get(state).values();
                if (!transitions.isEmpty()) {
                    for (String transition : transitions) {
                        // It is okay to have a transition out of the violating state if it directly returns to the
                        // violating state.
                        if (!transition.equals(state)) {
                            // not return to the violating state
                            throw new RuntimeException("TRANSITIONS OUT OF VIOLATING STATE!");
                        }
                    }
                }

                // there is no transition out of this state
                for (String event : monGenUtil.getEventNames()) {
                    MethodCallExpr expr = new MethodCallExpr(top, "ignore");
                    expr.addArgument("Event." + eventNameToIDMapping.get(event));
                    top = expr;
                }
                MethodCallExpr onEntry = new MethodCallExpr(top, "onEntry");
                onEntry.addArgument(monitorClassName + "::handler");
                top = onEntry;
            } else {
                for (Map.Entry<String, String> transition : monGenUtil.getTransitions().get(state).entrySet()) {
                    String event = transition.getKey();
                    String nextState = transition.getValue();
                    MethodCallExpr expr;
                    if (nextState.equals(state)) {
                        // this is a self-loop
                        expr = new MethodCallExpr(top, "permitReentry");
                        expr.addArgument("Event." + eventNameToIDMapping.get(event));
                    } else {
                        expr = new MethodCallExpr(top, "permit");
                        expr.addArgument("Event." + eventNameToIDMapping.get(event));
                        expr.addArgument("State." + nextState.toUpperCase());
                    }
                    top = expr;
                }
            }
            block.addStatement(top);
        }

        block.addStatement(new ReturnStmt(getStateMachineObject(block)));
        method.setBody(block);
        klass.addMember(method);
    }

    private Expression getStateMachineObject(BlockStmt blockStmt) {
        Expression returnStmt;
        FieldAccessExpr configField = new FieldAccessExpr(new ThisExpr(), "config");
        if (!monGenUtil.getDefaultStates().isEmpty()) {
            returnStmt = new ObjectCreationExpr(null,
                    new ClassOrInterfaceType("StateMachine<>"),
                    new NodeList<>(new NameExpr("State."
                            + new ArrayList<>(monGenUtil.getDefaultStates()).get(0).toUpperCase()), configField));
        } else if (monGenUtil.getCategory().equals("match") && monGenUtil.getStartState() != null) {
            returnStmt = new ObjectCreationExpr(null,
                    new ClassOrInterfaceType("StateMachine<>"),
                    new NodeList<>(new NameExpr("State."
                            + monGenUtil.getStartState().toUpperCase()), configField));
        } else if (monGenUtil.getCategory().equals("fail") && monGenUtil.getStartState() != null) {
            Statement assignStream = getStatement(configField);

            MethodCallExpr unhandled = new MethodCallExpr(new NameExpr("stateEventStateMachine"), "onUnhandledTrigger");
            ObjectCreationExpr anon = new ObjectCreationExpr(null,
                    new ClassOrInterfaceType("Action2<State, Event>"), new NodeList<>());

            MethodDeclaration doIt = new MethodDeclaration(
                    Modifier.createModifierList(Modifier.Keyword.PUBLIC), new VoidType(), "doIt");
            doIt.addAnnotation("Override");
            doIt.setParameters(new NodeList<>(new Parameter(new ClassOrInterfaceType("State"), "state"),
                    new Parameter(new ClassOrInterfaceType("Event"), "event")));

            MethodCallExpr callHandler  = new MethodCallExpr(null, "handler");
            callHandler.setArguments(new NodeList<>(new ObjectCreationExpr(null,
                    new ClassOrInterfaceType("Transition<>"),
                    new NodeList<>(new NameExpr("state"), new NameExpr("State.FAIL"), new NameExpr("event")))));

            BlockStmt doItBody = new BlockStmt();
            doItBody.addStatement(callHandler);
            doIt.setBody(doItBody);
            anon.setAnonymousClassBody(new NodeList<>(doIt));
            unhandled.setArguments(new NodeList<>(anon));
            blockStmt.addStatement(assignStream);
            blockStmt.addStatement(unhandled);
            returnStmt = new NameExpr("stateEventStateMachine");
        } else if ((monGenUtil.getCategory().equals("err") || monGenUtil.getCategory().equals("unsafe"))
                && monGenUtil.getStartState() != null) {
            returnStmt = new ObjectCreationExpr(null,
                    new ClassOrInterfaceType("StateMachine<>"),
                    new NodeList<>(new NameExpr("State."
                            + monGenUtil.getStartState().toUpperCase()), configField));
        } else {
            return null;
        }
        return returnStmt;
    }

    private Statement getStatement(FieldAccessExpr configField) {
        VariableDeclarationExpr returnMachine
                = new VariableDeclarationExpr(
                        new ClassOrInterfaceType("StateMachine<State, Event>"), "stateEventStateMachine");
        Statement assignStream = new ExpressionStmt(new AssignExpr(
                returnMachine,
                new ObjectCreationExpr(null,
                        new ClassOrInterfaceType("StateMachine<>"),
                        new NodeList<>(new NameExpr("State."
                                + monGenUtil.getStartState().toUpperCase()), configField)),
                AssignExpr.Operator.ASSIGN));
        return assignStream;
    }

    private boolean isAliasedToCategory(String state, String category) {
        boolean isAliased = false;
        if (monGenUtil.getAliasedStates().get(category).contains(state)) {
            isAliased = true;
        }
        return isAliased;
    }

    private void addConstructor(ClassOrInterfaceDeclaration klass) {
        String specName = "specName";
        String trace = "trace";
        String automaton = "automaton";
        String verdict = "verdict";
        klass.addConstructor(Modifier.Keyword.PUBLIC)
                .addParameter(String.class, specName)
                .setBody(new BlockStmt()
                        .addStatement(new ExpressionStmt(new AssignExpr(
                                new FieldAccessExpr(new ThisExpr(), specName),
                                new NameExpr(specName),
                                AssignExpr.Operator.ASSIGN)))
                        .addStatement(new ExpressionStmt(new AssignExpr(
                                new FieldAccessExpr(new ThisExpr(), trace),
                                new ObjectCreationExpr(null,
                                        new ClassOrInterfaceType("ArrayList<>"), new NodeList<>()),
                                AssignExpr.Operator.ASSIGN)))
                        .addStatement(new ExpressionStmt(new AssignExpr(
                                new FieldAccessExpr(new ThisExpr(), automaton),
                                new MethodCallExpr("buildAutomaton"),
                                AssignExpr.Operator.ASSIGN)))
                        .addStatement(new ExpressionStmt(new AssignExpr(
                                new FieldAccessExpr(new ThisExpr(), verdict),
                                new NameExpr("VerdictCategory.DONTKNOW"),
                                AssignExpr.Operator.ASSIGN))));
    }

    private void addFields(ClassOrInterfaceDeclaration klass) {
        klass.addField("List<Event>", "trace", Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
        klass.addField(String.class, "specName", Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);
        klass.addField("StateMachine<State, Event>", "automaton", Modifier.Keyword.PRIVATE);
        klass.addField("StateMachineConfig<State, Event>", "config", Modifier.Keyword.PRIVATE);
        klass.addField("VerdictCategory", "verdict", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
    }

    private void addImports(CompilationUnit code) {
        code.addImport("com.github.oxo42.stateless4j.StateMachine");
        code.addImport("com.github.oxo42.stateless4j.StateMachineConfig");
        code.addImport("com.github.oxo42.stateless4j.delegates.Action2");
        code.addImport("com.github.oxo42.stateless4j.transitions.Transition");
        code.addImport("edu.lazymop.tinymop.specparser.monitoring.RuntimeMonitor");
        code.addImport("java.io.FileOutputStream");
        code.addImport("java.io.IOException");
        code.addImport("java.util.ArrayList");
        code.addImport("java.util.Arrays");
        code.addImport("java.util.List");
    }

    private void addEnums(ClassOrInterfaceDeclaration klass) {
        EnumDeclaration stateEnum = new EnumDeclaration(Modifier.createModifierList(Modifier.Keyword.PRIVATE), "State");
        populateStateEnum(stateEnum, monGenUtil.getStates());
        EnumDeclaration eventEnum = new EnumDeclaration(Modifier.createModifierList(Modifier.Keyword.PRIVATE), "Event");
        populateEventEnum(eventEnum, monGenUtil.getEventNames());
        klass.addMember(stateEnum);
        klass.addMember(eventEnum);
    }

    private void populateStateEnum(EnumDeclaration enumDeclaration, Collection<String> states) {
        for (String state : states) {
            enumDeclaration.addEntry(new EnumConstantDeclaration(state.toUpperCase()));
        }

        if (monGenUtil.getCategory().equals("fail")) {
            enumDeclaration.addEntry(new EnumConstantDeclaration("FAIL"));
        }
    }

    private void populateEventEnum(EnumDeclaration enumDeclaration, Collection<String> events) {
        for (String event : events) {
            EnumConstantDeclaration declaration = new EnumConstantDeclaration(eventNameToIDMapping.get(event));
            declaration.setLineComment(event.toUpperCase());
            enumDeclaration.addEntry(declaration);
        }
    }
}
