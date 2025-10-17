// Based on JavaMOP's com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.event.itf.EventMethodBody

package edu.lazymop.tinymop.specparser.slicing.component.handler;

import java.util.HashSet;
import java.util.TreeMap;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.runtimeverification.rvmonitor.java.rvj.output.CodeGenerationOption;
import com.runtimeverification.rvmonitor.java.rvj.output.NotImplementedException;
import com.runtimeverification.rvmonitor.java.rvj.output.RVMVariable;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeAssignStmt;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeBinOpExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeCastExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeConditionStmt;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeInstanceOfExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeStmt;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeStmtCollection;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeVarDeclStmt;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeVarRefExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.helper.CodePair;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.helper.CodeVariable;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.type.CodeRVType;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.type.CodeType;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.GlobalLock;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingCacheNew;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingDeclNew;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingTreeImplementation;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingTreeInterface;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingTreeQueryResult;
import com.runtimeverification.rvmonitor.java.rvj.output.monitor.MonitorFeatures;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.EventDefinition;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameter;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameterSet;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameters;
import edu.lazymop.tinymop.specparser.slicing.component.EventHandler;
import edu.lazymop.tinymop.specparser.slicing.component.EventHandlerUtil;
import edu.lazymop.tinymop.specparser.slicing.component.handler.cache.IndexingCache;

public class EventMethodBody {

    protected final TreeMap<RVMParameters, IndexingTreeInterface> indexingTrees;

    private final EventHandlerUtil eventHandlerUtil;
    private final EventDefinition event;
    private final BlockStmt blockStmt;
    private final Strategy strategy;
    private final IndexingTree indexingTreeGen;
    private boolean isSecondPass;

    public EventMethodBody(EventDefinition event, BlockStmt blockStmt, EventHandlerUtil eventHandlerUtil,
                           boolean isSecondPass) {
        this.eventHandlerUtil = eventHandlerUtil;
        this.event = event;
        this.blockStmt = blockStmt;
        this.isSecondPass = isSecondPass;

        IndexingDeclNew indexingDecl = eventHandlerUtil.indexingTreeManager
                .getIndexingDecl(eventHandlerUtil.getRVMonitorSpec());
        this.indexingTrees = indexingDecl.getIndexingTrees();

        boolean isGeneral = eventHandlerUtil.getRVMonitorSpec().isGeneral();
        boolean isFullyBound = this.event.getParameters().contains(eventHandlerUtil.getRVMonitorSpec().getParameters());

        this.strategy = new Strategy(this.event.isStartEvent(), isGeneral, isFullyBound, event.getRVMParametersOnSpec(),
                eventHandlerUtil.disableParams);

        this.indexingTreeGen = new IndexingTree(
                this.indexingTrees,
                indexingDecl.getIndexingTreesForCopy(),
                eventHandlerUtil.monitors.get(eventHandlerUtil.getRVMonitorSpec()),
                indexingDecl.getCopyParamForEvent(event),
                event.getRVMParametersOnSpec(),
                eventHandlerUtil.getRVMonitorSpec(),
                event,
                eventHandlerUtil.indexingTreeManager
        );

        MonitorFeatures features = this.indexingTreeGen.monitorClass.getFeatures();
        features.setTimeTracking(this.strategy.needsTimeTracking);
        features.setDisableHolder(this.strategy.needsTimeTracking);
    }

    private CustomWeakReferenceVariables getWeakReferenceVariables(EventDefinition event) {
        return new CustomWeakReferenceVariables(eventHandlerUtil.indexingTreeManager, event.getRVMParametersOnSpec());
    }

    private CustomIndexingTreeQueryResult getIndexingTreeQueryResult() {
        CustomWeakReferenceVariables weakRefs = getWeakReferenceVariables(event);
        return new CustomIndexingTreeQueryResult(
                indexingTrees.get(event.getRVMParametersOnSpec()), weakRefs, event.getRVMParametersOnSpec(),
                IndexingTreeImplementation.Access.Entry, "matched"
        );
    }

    /**
     * Generates the initialization code for weak references and adds it to the given block statement.
     */
    public void getWeakReferenceInit() {
        CustomWeakReferenceVariables weakRefs = getWeakReferenceVariables(event);
        weakRefs.getDeclarationCode(blockStmt);
    }

    /**
     * Generates the initialization code for indexing tree and adds it to the given block statement.
     */
    public void getMatchedIndexingTreeInit() {
        CustomIndexingTreeQueryResult transition = getIndexingTreeQueryResult();
        transition.getDeclarationCode(blockStmt);
    }

    /**
     * Generates the cache retrieval code for the given event and adds it to the given block statement.
     */
    public void getCache() {
        // Initially, we set cachehit to false
        blockStmt.addStatement(new ExpressionStmt(new VariableDeclarationExpr(new VariableDeclarator(
                PrimitiveType.booleanType(), "cachehit", new BooleanLiteralExpr(false)))));

        IndexingTreeInterface itf = indexingTrees.get(event.getRVMParametersOnSpec());
        IndexingCache cache = IndexingCache.fromTree(
                itf.getName().replace(eventHandlerUtil.getRVMonitorSpec().getName() + "_", ""), itf);
        if (cache == null) {
            blockStmt.addStatement(getIndexingTreeLookup());
            return;
        }

        BinaryExpr condition = cache.getCacheCondition();
        blockStmt.addStatement(new IfStmt().setCondition(condition)
                        .setThenStmt(cache.getCacheRetrievalCode(getIndexingTreeQueryResult().getEntryRef()))
                        .setElseStmt(getIndexingTreeLookup()));

        getIndexingTreeLookup();
    }

    public void updateCache(BlockStmt blockStmt) { // similar to generateCacheUpdate in JavaMOP
        IndexingTreeInterface itf = indexingTrees.get(event.getRVMParametersOnSpec());
        IndexingCache cache = IndexingCache.fromTree(
                itf.getName().replace(eventHandlerUtil.getRVMonitorSpec().getName() + "_", ""), itf);
        if (cache == null) {
            return;
        }

        BinaryExpr isCacheMissed = new BinaryExpr(new NameExpr("cachehit"), new BooleanLiteralExpr(false),
                BinaryExpr.Operator.EQUALS);
        blockStmt.addStatement(new IfStmt().setCondition(isCacheMissed)
                .setThenStmt(cache.getCacheUpdateCode(getIndexingTreeQueryResult().getEntryRef())));
    }

    private BlockStmt getIndexingTreeLookup() {
        BlockStmt lookupBlock = new BlockStmt();

        /*
        TODO: Check if we need this
        {
            IndexingCacheNew cache = this.getIndexingTreeCache();
            if (cache != null)
                stmts.add(this.getBehaviorObserver()
                        .generateIndexingTreeCacheMissedCode(cache));
        }
         */
        CustomIndexingTreeQueryResult matched = getIndexingTreeQueryResult();
        if (this.strategy.needsWeakReferenceLookup) {
            IndexingTree.generateWeakReferenceLookup(event.getRVMParametersOnSpec(), matched.getWeakRefs(), false,
                    lookupBlock);
        }

        IndexingTreeImplementation.StmtCollectionInserter<CodeExpr> inserter = new IndexingTreeImplementation
                .StmtCollectionInserter<CodeExpr>() {
            @Override
            public CodeStmtCollection insertSecondLastMap(CodeExpr mapref) {
                if (matched.getLastMapRef() == null) {
                    return null;
                }
                CodeStmt assign = new CodeAssignStmt(
                        matched.getLastMapRef(), mapref);
                return new CodeStmtCollection(assign);
            }

            @Override
            public CodeStmtCollection insertLastEntry(IndexingTreeImplementation.Entry entry,
                                                      CodeExpr entryref) {
                CodeStmt assign = new CodeAssignStmt(matched.getEntryRef(),
                        entryref);
                return new CodeStmtCollection(assign);
            }
        };

        IndexingTreeInterface indexingTree = indexingTrees.get(event.getRVMParametersOnSpec());
        if (this.strategy.shouldCreateIndexingTreeIntemediateNodes) {
            // TODO: Just a quick hack
            for (Statement stmts : StaticJavaParser.parseBlock(indexingTree.generateFindOrCreateEntryCode(
                    matched.getWeakRefs(), inserter).toString()).getStatements()) {
                lookupBlock.addStatement(stmts);
            }
        } else {
            for (Statement stmts : StaticJavaParser.parseBlock(indexingTree.generateFindEntryWithStrongRefCode(
                    matched.getWeakRefs(), inserter, true).toString()).getStatements()) {
                lookupBlock.addStatement(stmts);
            }
        }
        /*
        TODO: Check if we need this
        stmts.add(this.getBehaviorObserver()
                    .generateIndexingTreeLookupCode(indexingtree,
                            LookupPurpose.TransitionedMonitor,
                            matched.getWeakRefs(),
                            !this.strategy.needsWeakReferenceLookup,
                            matched.getEntryRef()));
         */

        return lookupBlock;
    }

    public void getMonitorCreation() {
        if (!this.strategy.mayCreateMonitors) {
            return;
        }

        CustomIndexingTreeQueryResult transition = getIndexingTreeQueryResult();
        CodePair<CodeVarRefExpr> codepair = transition.generateFieldGetCode(
                IndexingTreeImplementation.Access.Leaf, "matched");

        if (codepair.getGeneratedCode() != null) {
            Statement monStmt = StaticJavaParser.parseStatement(codepair.getGeneratedCode().toString());
            blockStmt.addStatement(monStmt);
        }

        String varName = codepair.getLogicalReturn().getVariable().getName();
        BinaryExpr isEqualNull = new BinaryExpr(new NameExpr(varName), new NullLiteralExpr(), BinaryExpr.Operator.EQUALS);
        BlockStmt ifBody1 = new BlockStmt();
        blockStmt.addStatement(new IfStmt().setCondition(isEqualNull).setThenStmt(ifBody1));

        // TODO: something is wrong here
        if (!CodeGenerationOption.isCacheKeyWeakReference()) {
            IndexingTree.generateWeakReferenceLookup(event.getRVMParametersOnSpec(), transition.getWeakRefs(), true,
                    ifBody1);
        }

        boolean maynotnull = false;
        // TODO: Just a quick hack
        if (this.strategy.mayCopyFromOtherMonitors) {
            if (isSecondPass) {
                String javamopOutput = indexingTreeGen.generateCreateNewMonitorStateCode(transition).toString();
                if (!javamopOutput.trim().isEmpty()) {
                    for (Statement stmts : StaticJavaParser.parseBlock(javamopOutput).getStatements()) {
                        ifBody1.addStatement(stmts);
                    }
                }
            } else {
                indexingTreeGen.generateCreateNewMonitorStateCode(transition);
            }

            maynotnull = true;
        }

        if (this.event.isStartEvent()) {
            BlockStmt ifBody2 = new BlockStmt();
            if (isSecondPass) {
                String javamopOutput = indexingTreeGen.generateDefineNewCode(transition, strategy).toString();

                if (!javamopOutput.trim().isEmpty()) {
                    for (Statement stmts : StaticJavaParser.parseBlock("{" + javamopOutput + "}").getStatements()) {
                        ifBody2.addStatement(stmts);
                    }

                    if (maynotnull) {
                        ifBody1.addStatement(new IfStmt().setCondition(isEqualNull).setThenStmt(ifBody2));
                    } else {
                        ifBody1.addStatement(ifBody2);
                    }
                }
            } else {
                indexingTreeGen.generateDefineNewCode(transition, strategy);
            }
        }

        if (this.strategy.needsTimeTracking) {
            String javamopOutput = indexingTreeGen.generateDisableUpdateCode(transition).toString();
            if (!javamopOutput.trim().isEmpty()) {
                for (Statement stmts : StaticJavaParser.parseBlock("{" + javamopOutput + "}").getStatements()) {
                    ifBody1.addStatement(stmts);
                }
            }
        }
    }

    public void updateSlices() { // similar to generateMonitorTransition in JavaMOP
        CustomIndexingTreeQueryResult transition = getIndexingTreeQueryResult();
        boolean single = this.event.getParameters().contains(
                this.eventHandlerUtil.getRVMonitorSpec().getParameters()
        );

        BinaryExpr guard1cond = null;
        BlockStmt guard1body = new BlockStmt();
        CodeVarRefExpr affectedref;
        if (this.strategy.needsNullCheckForTransition) {
            if (transition.getEntryRef().getType() instanceof CodeRVType.Tuple) {
                guard1cond = new BinaryExpr(new NameExpr(transition.getEntryRef().getVariable().getName()),
                        new NullLiteralExpr(), BinaryExpr.Operator.NOT_EQUALS);
            }
        }

        IndexingTreeImplementation.Access access = single
                ? IndexingTreeImplementation.Access.Leaf : IndexingTreeImplementation.Access.Set;
        CodePair<CodeVarRefExpr> pair = transition.generateFieldGetCode(access, "stateTransitioned");

        CodeStmtCollection javamopOutput = pair.getGeneratedCode();
        if (javamopOutput != null) {
            for (Statement stmts : StaticJavaParser.parseBlock("{" + javamopOutput + "}").getStatements()) {
                guard1body.addStatement(stmts);
            }
        }
        affectedref = pair.getLogicalReturn();
        String affectedrefVar = affectedref.getVariable().getName(); // matchedEntry or stateTransitionedSet

        Expression guard2cond = null;
        if (this.strategy.needsNullCheckForTransition) {
            guard2cond = new BinaryExpr(new NameExpr(affectedref.getVariable().getName()),
                    new NullLiteralExpr(), BinaryExpr.Operator.NOT_EQUALS);
        }

        BlockStmt guard2body = new BlockStmt();
        NodeList<Expression> arguments = new NodeList<>();
        for (RVMParameter parameter : event.getParameters()) {
            arguments.add(new NameExpr(parameter.getName()));
        }
        arguments.add(new NameExpr("event"));

        if (single) {
            // Single monitor: check if entry is an actual slice (and not a disable holder)
            // then, cast slice to actual type, then call the event handler of that slice.
            CodeType leaftype = affectedref.getVariable().getType();

            if (leaftype instanceof CodeRVType.DisableHolder) {
                throw new NotImplementedException();
            } else if (leaftype instanceof CodeRVType.Interface) {
                // CollectionsSynchronizedCollectionMonitor
                String monitorType = ((CodeRVType.Interface) leaftype).getMonitorType().getJavaType();

                // e.g., matchedEntry instanceof CollectionsSynchronizedCollectionMonitor
                guard2cond = new InstanceOfExpr(new NameExpr(affectedref.getVariable().getName()),
                        new ClassOrInterfaceType(null, monitorType));

                // e.g.,
                // CollectionsSynchronizedCollectionMonitor monitor=(CollectionsSynchronizedCollectionMonitor)matchedEntry;
                Type type = new ClassOrInterfaceType(null, monitorType);
                CastExpr castExpr = new CastExpr(type, new NameExpr(affectedref.getVariable().getName()));
                VariableDeclarationExpr varDecl = new VariableDeclarationExpr(new VariableDeclarator(type,
                        "monitor", castExpr));
                guard2body.addStatement(new ExpressionStmt(varDecl));
                affectedrefVar = "monitor"; // change affectedrefVar from matchedEntry to monitor
            }

            MethodCallExpr methodCall = new MethodCallExpr(new NameExpr(affectedrefVar), event.getId(), arguments);
            guard2body.addStatement(new ExpressionStmt(methodCall));
        } else {
            MethodCallExpr methodCall = new MethodCallExpr(new NameExpr(affectedrefVar), event.getId(), arguments);
            guard2body.addStatement(new ExpressionStmt(methodCall));
        }

        // Add cache (similar to JavaMOP's guard2body.add(cacheupdatecode))
        updateCache(guard2body);

        if (guard2cond != null) {
            guard1body.addStatement(new IfStmt().setCondition(guard2cond).setThenStmt(guard2body));
        } else {
            for (Statement stmts : guard2body.getStatements()) {
                guard1body.addStatement(stmts);
            }
        }

        if (guard1cond != null) {
            blockStmt.addStatement(new IfStmt().setCondition(guard1cond).setThenStmt(guard1body));
        } else {
            for (Statement stmts : guard1body.getStatements()) {
                blockStmt.addStatement(stmts);
            }
        }
    }
}
