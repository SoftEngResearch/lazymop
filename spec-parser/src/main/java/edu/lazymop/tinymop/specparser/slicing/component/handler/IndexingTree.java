// Based on JavaMOP's com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.event.itf.EventMethodBody

package edu.lazymop.tinymop.specparser.slicing.component.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.runtimeverification.rvmonitor.java.rt.observable.IInternalBehaviorObserver;
import com.runtimeverification.rvmonitor.java.rvj.Main;
import com.runtimeverification.rvmonitor.java.rvj.output.CodeGenerationOption;
import com.runtimeverification.rvmonitor.java.rvj.output.NotImplementedException;
import com.runtimeverification.rvmonitor.java.rvj.output.RVMVariable;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeAssignStmt;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeBinOpExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeBlockStmt;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeCastExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeConditionStmt;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeExprStmt;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeFieldRefExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeForStmt;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeInstanceOfExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeLazyStmt;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeLiteralExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeMemberField;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeMethodInvokeExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeNegExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeNewExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodePrePostfixExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeStmt;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeStmtCollection;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeVarDeclStmt;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeVarRefExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.helper.CodeHelper;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.helper.CodePair;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.helper.CodeVariable;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.type.CodeRVType;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.type.CodeType;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.InternalBehaviorObservableCodeGenerator;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.RuntimeServiceManager;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.event.itf.WeakReferenceVariables;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.indexingtree.IndexingTreeManager;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingTreeImplementation;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingTreeInterface;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingTreeQueryResult;
import com.runtimeverification.rvmonitor.java.rvj.output.monitor.MonitorFeatures;
import com.runtimeverification.rvmonitor.java.rvj.output.monitor.MonitorInfo;
import com.runtimeverification.rvmonitor.java.rvj.output.monitor.SuffixMonitor;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.EventDefinition;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameter;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameters;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMonitorParameterPair;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMonitorSpec;

public class IndexingTree {

    public final IndexingTreeManager indexingTreeManager;

    protected final TreeMap<RVMParameters, IndexingTreeInterface> indexingTrees;
    protected final SuffixMonitor monitorClass;
    protected final RVMParameters eventParams;
    protected final RVMonitorSpec rvmSpec;
    protected final EventDefinition event;

    private final Map<RVMonitorParameterPair, IndexingTreeInterface> indexingTreesForCopy;
    private final List<RVMonitorParameterPair> paramPairsForCopy;

    public IndexingTree(TreeMap<RVMParameters, IndexingTreeInterface> indexingTrees,
                        Map<RVMonitorParameterPair, IndexingTreeInterface> indexingTreesForCopy,
                        SuffixMonitor monitorClass, List<RVMonitorParameterPair> paramPairsForCopy,
                        RVMParameters eventParams, RVMonitorSpec rvmSpec, EventDefinition event,
                        IndexingTreeManager indexingTreeManager) {
        this.indexingTrees = indexingTrees;
        this.indexingTreesForCopy = indexingTreesForCopy;
        this.monitorClass = monitorClass;
        this.paramPairsForCopy = paramPairsForCopy;
        this.eventParams = eventParams;
        this.rvmSpec = rvmSpec;
        this.event = event;
        this.indexingTreeManager = indexingTreeManager;
    }


    public static void generateWeakReferenceLookup(
            RVMParameters eventParams,
            WeakReferenceVariables weakRefs, boolean conditional, BlockStmt blockStmt) {
        for (RVMParameter param : eventParams) {
            if (CodeGenerationOption.isCacheKeyWeakReference()) {
                throw new NotImplementedException();
            }
            CodeType weakRefType = CodeHelper.RuntimeType.getWeakReference();

            ObjectCreationExpr newCachedWeakRefs = new ObjectCreationExpr(null,
                    new ClassOrInterfaceType().setName(weakRefType.getJavaType()),
                    new NodeList<>(new NameExpr(param.getName()))
            );

            AssignExpr assign = new AssignExpr(new NameExpr(weakRefs.getWeakRef(param).getName()), newCachedWeakRefs,
                    AssignExpr.Operator.ASSIGN
            );

            if (!conditional) {
                blockStmt.addStatement(assign);
            } else {
                CodeVarRefExpr weakref = new CodeVarRefExpr(weakRefs.getWeakRef(param));

                BinaryExpr condition = new BinaryExpr(new NameExpr(weakref.getVariable().getName()), new NullLiteralExpr(),
                        BinaryExpr.Operator.EQUALS);
                blockStmt.addStatement(new IfStmt().setCondition(condition).setThenStmt(new ExpressionStmt(assign)));
            }
        }
    }

    public InternalBehaviorObservableCodeGenerator getBehaviorObserver() {
        return new RuntimeServiceManager().getObserver();
    }

    private CodeStmtCollection generateCopyStateFromMonitorCode(
            RVMParameters sourceprms, RVMParameters targetprms,
            IndexingTreeQueryResult transition, IndexingTreeQueryResult source) {
        return this.generateCopyStateInternalCode(sourceprms, targetprms,
                transition, source.getLeafRef(), transition, true);
    }

    private CodeExpr generateTimeCheckCode(CodeVarRefExpr source,
                                           CodeExpr candidate) {
        CodeExpr t1 = new CodeMethodInvokeExpr(CodeType.integer(), source,
                "getTau");
        CodeExpr t2 = new CodeMethodInvokeExpr(CodeType.integer(), candidate,
                "getTau");
        CodeExpr disable2 = new CodeMethodInvokeExpr(CodeType.integer(),
                candidate, "getDisable");

        CodeExpr cond1 = CodeBinOpExpr.greater(disable2, t1);

        CodeExpr cond2 = CodeBinOpExpr.less(t2, t1);
        CodeExpr cond2extra = CodeBinOpExpr.greater(t2,
                CodeLiteralExpr.integer(0));
        cond2 = CodeBinOpExpr.logicalAnd(cond2extra, cond2);

        CodeExpr cond = CodeBinOpExpr.logicalOr(cond1, cond2);
        return cond;
    }

    private CodeType getMonitorType() {
        return new CodeType(this.monitorClass.getOutermostName().getVarName());
    }

    private CodeStmtCollection generateCopyStateInternalCode(
            RVMParameters sourceprms, RVMParameters targetprms,
            final IndexingTreeQueryResult dest,
            final CodeVarRefExpr sourcemonref,
            IndexingTreeQueryResult transition, boolean isFromMonitor) {
        CodeStmtCollection stmts = new CodeStmtCollection();

        CodeVariable definable;
        definable = new CodeVariable(CodeType.bool(), "definable");
        CodeVarDeclStmt decl = new CodeVarDeclStmt(definable,
                CodeLiteralExpr.bool(true));
        stmts.add(decl);
        final CodeVarRefExpr definableref = new CodeVarRefExpr(definable);

        // param := theta'' in defineTo in D(X)
        for (RVMParameters param : this.indexingTrees.keySet()) {
            if (targetprms.contains(param) && !sourceprms.contains(param)) {
                stmts.comment("D(X) defineTo:1--5 for <"
                        + param.parameterString() + ">");
                CodeExpr ifcond = definableref;
                CodeStmtCollection ifbody = new CodeStmtCollection();
                final IndexingTreeInterface srctree = this.indexingTrees
                        .get(param);

                IndexingTreeImplementation.StmtCollectionInserter<CodeExpr> inserter = new IndexingTreeImplementation
                        .StmtCollectionInserter<CodeExpr>() {
                    @Override
                    public CodeStmtCollection insertLastField(IndexingTreeImplementation.Entry entry,
                                                              CodeExpr leafref) {
                        CodeStmtCollection stmts = new CodeStmtCollection();
                        CodeStmtCollection guarded = stmts;

                        if (entry.getCodeType() instanceof CodeRVType.Tuple) {
                            // If this is of tuple type, the corresponding
                            // field should be additionally checked.
                            CodeExpr ifextracond = CodeBinOpExpr
                                    .isNotNull(leafref);
                            guarded = new CodeStmtCollection();
                            stmts.add(new CodeConditionStmt(ifextracond,
                                    guarded));
                        }

                        CodeStmt check = new CodeConditionStmt(
                                generateTimeCheckCode(sourcemonref, leafref),
                                new CodeAssignStmt(definableref,
                                        CodeLiteralExpr.bool(false)));
                        guarded.add(check);
                        guarded.add(getBehaviorObserver()
                                .generateTimeCheckedCode(srctree,
                                        dest.getWeakRefs(), sourcemonref,
                                        leafref, definableref));

                        return stmts;
                    }
                };
                ifbody.add(srctree.generateFindCode(IndexingTreeImplementation.Access.Leaf,
                        dest.getWeakRefs(), inserter));
                stmts.add(new CodeConditionStmt(ifcond, ifbody));
            }
        }

        CodeExpr ifcond = definableref;
        CodeStmtCollection ifbody = new CodeStmtCollection();
        stmts.add(new CodeConditionStmt(ifcond, ifbody));

        CodeVarRefExpr monitorref;
        ifbody.comment("D(X) defineTo:6");
        CodeVarDeclStmt decl2 = new CodeVarDeclStmt(new CodeVariable(
                this.getMonitorType(), "created"), new CodeCastExpr(
                this.getMonitorType(), new CodeMethodInvokeExpr(
                CodeType.object(), sourcemonref, "clone")));
        ifbody.add(decl2);
        monitorref = new CodeVarRefExpr(decl2.getVariable());

        MonitorWeakRefSetLazyCode weakrefset = new MonitorWeakRefSetLazyCode(
                this.getMonitorFeatures(), sourceprms, targetprms,
                dest.getWeakRefs(), monitorref);
        ifbody.add(weakrefset);

        ifbody.add(this.getBehaviorObserver().generateMonitorClonedCode(
                sourcemonref, monitorref));

        MonitorInfo moninfo = this.getMonitorInfo();
        if (moninfo != null) {
            String legacycode = moninfo.expand(monitorref.getVariable()
                    .toLegacy(), this.monitorClass, targetprms);
            ifbody.add(CodeStmtCollection.fromLegacy(legacycode));
        }

        ifbody.add(this.generateInsertMonitorCode(dest, transition,
                monitorref, true, isFromMonitor));

        return stmts;
    }

    private CodeStmtCollection generateInsertMonitorCode(
            IndexingTreeQueryResult created,
            IndexingTreeQueryResult transition, CodeVarRefExpr monitorref,
            boolean isDefineTo, boolean isFromMonitor) {
        CodeStmtCollection stmts = new CodeStmtCollection();

        boolean forceleafupdate = false;
        if (!isDefineTo || isFromMonitor) {
            forceleafupdate = true;
        }
        stmts.add(created.generateLeafUpdateCode(monitorref, forceleafupdate));

        if (isFromMonitor) {
            if (transition.getEntry().getCodeType() instanceof CodeRVType.Tuple) {
                // If the type is not a tuple, it must be a leaf. In such case,
                // 'entry' is
                // the same as 'leaf', and assigning has been already done.
                stmts.add(new CodeAssignStmt(transition.getLeafRef(),
                        monitorref));
            }

            if (transition.getEntry().getSet() != null) {
                CodePair<CodeVarRefExpr> codepair = transition
                        .generateFieldGetCode(IndexingTreeImplementation.Access.Set, "enclosing");
                stmts.add(codepair.getGeneratedCode());

                CodeVarRefExpr fieldref = codepair.getLogicalReturn();
                CodeMethodInvokeExpr invoke = new CodeMethodInvokeExpr(
                        CodeType.foid(), fieldref, "add", monitorref);
                stmts.add(new CodeExprStmt(invoke));
            }
        }

        if (!isDefineTo) {
            // When this method is reached for cloning monitors, this branch is
            // not taken. This is because
            // the matched entry (which is referred to by 'entry') has nothing
            // to do with the created monitor.
            if (created.getEntry().getSet() != null) {
                CodePair<CodeVarRefExpr> codepair = created
                        .generateFieldGetCode(IndexingTreeImplementation.Access.Set, "enclosing");
                stmts.add(codepair.getGeneratedCode());

                CodeVarRefExpr fieldref = codepair.getLogicalReturn();
                CodeMethodInvokeExpr invoke = new CodeMethodInvokeExpr(
                        CodeType.foid(), fieldref, "add", monitorref);
                stmts.add(new CodeExprStmt(invoke));
            }
        }

        stmts.add(this.generateInsertMonitorToAllCompatibleTreesCode(created,
                monitorref, isDefineTo));

        return stmts;
    }

    private CodeStmtCollection generateInsertMonitorToAllCompatibleTreesCode(
            IndexingTreeQueryResult combined, CodeVarRefExpr monitorref,
            boolean isDefineTo) {
        CodeStmtCollection stmts = new CodeStmtCollection();

        String ctxmsg = "D(X) " + (isDefineTo ? "defineTo:7" : "defineNew:5");

        RVMParameters targetprms = combined.getParams();
        WeakReferenceVariables weakrefs = combined.getWeakRefs();

        for (RVMParameters param : this.indexingTrees.keySet()) {
            if (targetprms.contains(param) && !targetprms.equals(param)) {
                stmts.comment(ctxmsg + " for <" + param.parameterString() + ">");
                IndexingTreeInterface srctree = this.indexingTrees.get(param);
                CodeStmtCollection insert = srctree.generateInsertMonitorCode(
                        weakrefs, monitorref);
                stmts.add(insert);
                stmts.add(this.getBehaviorObserver()
                        .generateIndexingTreeNodeInsertedCode(srctree,
                                weakrefs, monitorref));
            }
        }

        for (RVMonitorParameterPair pair : this.indexingTreesForCopy.keySet()) {
            if (targetprms.equals(pair.getParam2())) {
                stmts.comment(ctxmsg + " for <"
                        + pair.getParam1().parameterString() + "-"
                        + pair.getParam2().parameterString() + ">");
                IndexingTreeInterface srctree = this.indexingTreesForCopy
                        .get(pair);
                CodeStmtCollection insert = srctree.generateInsertMonitorCode(
                        weakrefs, monitorref);
                stmts.add(insert);
                stmts.add(this.getBehaviorObserver()
                        .generateIndexingTreeNodeInsertedCode(srctree,
                                weakrefs, monitorref));
            }
        }

        return stmts;
    }

    public CodeStmtCollection generateCreateNewMonitorStateCode(
            IndexingTreeQueryResult transition) {
        CodeStmtCollection stmts = new CodeStmtCollection();
        for (RVMonitorParameterPair pair : this.paramPairsForCopy) {
            boolean frommonitor = this.event.getRVMParametersOnSpec().contains(
                    pair.getParam2());
            IndexingTreeInterface sourcetree = this
                    .findIndexingTreeForCopy(pair);
            if (frommonitor && sourcetree == null) {
                sourcetree = this.findIndexingTree(pair);
            }
            IndexingTreeImplementation.Access access = frommonitor
                    ? IndexingTreeImplementation.Access.Leaf : IndexingTreeImplementation.Access.Set;

            CodeStmtCollection nested = new CodeStmtCollection();
            nested.comment("D(X) createNewMonitorStates:4 when Dom(theta'') = <"
                    + sourcetree.getQueryParams().parameterString() + ">");

            final IndexingTreeQueryResult sourceresult = new IndexingTreeQueryResult(
                    sourcetree, transition.getWeakRefs(),
                    sourcetree.getQueryParams(), access, "source");
            nested.add(sourceresult.generateDeclarationCode());

            IndexingTreeImplementation.StmtCollectionInserter<CodeExpr> inserter = new IndexingTreeImplementation
                    .StmtCollectionInserter<CodeExpr>() {
                @Override
                public CodeStmtCollection insertLastField(IndexingTreeImplementation.Entry entry,
                                                          CodeExpr leafref) {
                    CodeAssignStmt assign = new CodeAssignStmt(
                            sourceresult.getSetOrLeafRef(), leafref);
                    return new CodeStmtCollection(assign);
                }
            };
            nested.add(sourcetree.generateFindCode(access,
                    sourceresult.getWeakRefs(), inserter));
            nested.add(this.getBehaviorObserver()
                    .generateIndexingTreeLookupCode(sourcetree,
                            IInternalBehaviorObserver.LookupPurpose.ClonedMonitor,
                            sourceresult.getWeakRefs(), false,
                            sourceresult.getSetOrLeafRef()));

            RVMParameters sourceprms = pair.getParam2();
            RVMParameters targetprms = this.rvmSpec.getParameters()
                    .sortParam(
                            RVMParameters.unionSet(sourceprms,
                                    this.eventParams));

            CodeExpr ifcond = CodeBinOpExpr.isNotNull(sourceresult
                    .getSetOrLeafRef());
            CodeStmtCollection ifbody;
            if (frommonitor) {
                ifbody = this.generateCopyStateFromMonitorCode(sourceprms,
                        targetprms, transition, sourceresult);
            } else {
                ifbody = this.generateCopyStateFromListCode(sourceprms,
                        targetprms, transition, sourceresult);
            }
            nested.add(new CodeConditionStmt(ifcond, ifbody));
            stmts.add(new CodeBlockStmt(nested));
        }
        return stmts;
    }

    private CodeStmtCollection generateCopyStateFromListCode(
            RVMParameters sourceprms, RVMParameters targetprms,
            IndexingTreeQueryResult transition, IndexingTreeQueryResult source) {
        CodeStmtCollection stmts = new CodeStmtCollection();

        IndexingTreeInterface targettree = this.findIndexingTree(targetprms);

        WeakReferenceVariables borrowedweakrefs;
        RVMParameters borrowedprms = new RVMParameters();
        for (RVMParameter prm : sourceprms) {
            if (!this.eventParams.contains(prm)) {
                borrowedprms.add(prm);
                this.getMonitorFeatures().addRememberedParameters(prm);
            }
        }
        borrowedweakrefs = new WeakReferenceVariables(
                this.indexingTreeManager, borrowedprms);

        CodeVarDeclStmt declnumalive = new CodeVarDeclStmt(
                new CodeVariable(CodeType.integer(), "numalive"),
                CodeLiteralExpr.integer(0));
        CodeVarRefExpr numaliveref = new CodeVarRefExpr(
                declnumalive.getVariable());
        stmts.add(declnumalive);

        CodeVarDeclStmt declsize = new CodeVarDeclStmt(new CodeVariable(
                CodeType.integer(), "setlen"), new CodeMethodInvokeExpr(
                CodeType.integer(), source.getSetRef(), "getSize"));
        stmts.add(declsize);
        CodeVarRefExpr sizeref = new CodeVarRefExpr(declsize.getVariable());
        CodeVarDeclStmt decli = new CodeVarDeclStmt(new CodeVariable(
                CodeType.integer(), "ielem"), CodeLiteralExpr.integer(0));
        CodeVarRefExpr iref = new CodeVarRefExpr(decli.getVariable());
        CodeExpr cond = CodeBinOpExpr.less(iref, sizeref);
        CodeStmt incri = new CodeExprStmt(CodePrePostfixExpr.prefix(iref,
                true));
        CodeStmtCollection loopbody = new CodeStmtCollection();
        CodeForStmt loop = new CodeForStmt(decli, cond, incri, loopbody);
        stmts.add(loop);


        CodeVarDeclStmt declsrcmon = new CodeVarDeclStmt(
                new CodeVariable(this.getMonitorType(),
                        "sourceMonitor", "theta''"),
                new CodeMethodInvokeExpr(this.getMonitorType(), source
                        .getSetRef(), "get", iref));
        loopbody.add(declsrcmon);
        CodeVarRefExpr srcmonref = new CodeVarRefExpr(
                declsrcmon.getVariable());

        CodeExpr ifalive = new CodeNegExpr(new CodeMethodInvokeExpr(
                CodeType.bool(), srcmonref, "isTerminated"));
        for (Map.Entry<RVMParameter, CodeVariable> pair : borrowedweakrefs
                .getMapping().entrySet()) {
            CodeType wrtype = pair.getValue().getType();
            RVMVariable wrfieldname = this.monitorClass
                    .getRVMonitorRef(pair.getKey());
            CodeMemberField wrfield = new CodeMemberField(
                    wrfieldname.getVarName(), false, false, false,
                    wrtype);
            CodeFieldRefExpr wrfieldref = new CodeFieldRefExpr(
                    srcmonref, wrfield);
            CodeMethodInvokeExpr getref = new CodeMethodInvokeExpr(
                    CodeType.object(), wrfieldref, "get");
            CodeExpr notnull = CodeBinOpExpr.isNotNull(getref);
            ifalive = CodeBinOpExpr.logicalAnd(ifalive, notnull);
        }
        CodeStmtCollection alivebody = new CodeStmtCollection();
        loopbody.add(new CodeConditionStmt(ifalive, alivebody));


        CodeExpr incrnumalive = CodePrePostfixExpr.postfix(
                numaliveref, true);
        CodeMethodInvokeExpr move = new CodeMethodInvokeExpr(
                CodeType.foid(), source.getSetRef(), "set",
                incrnumalive, srcmonref);
        alivebody.add(new CodeExprStmt(move));

        alivebody.add(borrowedweakrefs.getDeclarationCode(
                this.monitorClass, srcmonref));
        WeakReferenceVariables mergedweakrefs = WeakReferenceVariables
                .merge(source.getWeakRefs(), borrowedweakrefs);

        final IndexingTreeQueryResult dest = new IndexingTreeQueryResult(
                targettree, mergedweakrefs, targetprms,
                IndexingTreeImplementation.Access.Leaf, "dest");
        alivebody.add(dest.generateDeclarationCode());

        IndexingTreeImplementation.StmtCollectionInserter<CodeExpr> inserter = new IndexingTreeImplementation
                .StmtCollectionInserter<CodeExpr>() {
            @Override
            public CodeStmtCollection insertSecondLastMap(
                    CodeExpr mapref) {
                CodeStmt assign = new CodeAssignStmt(
                        dest.getLastMapRef(), mapref);
                return new CodeStmtCollection(assign);
            }

            @Override
            public CodeStmtCollection insertLastEntry(IndexingTreeImplementation.Entry entry,
                                                      CodeExpr entryref) {
                CodeStmt assign = new CodeAssignStmt(
                        dest.getEntryRef(), entryref);
                return new CodeStmtCollection(assign);
            }

            @Override
            public CodeStmtCollection insertLastField(IndexingTreeImplementation.Entry entry,
                                                      CodeExpr leafref) {
                CodeStmt assign = new CodeAssignStmt(
                        dest.getLeafRef(), leafref);
                return new CodeStmtCollection(assign);
            }
        };
        alivebody.add(targettree.generateFindOrCreateCode(
                IndexingTreeImplementation.Access.Leaf, mergedweakrefs, inserter));
        alivebody.add(this.getBehaviorObserver()
                .generateIndexingTreeLookupCode(targettree,
                        IInternalBehaviorObserver.LookupPurpose.CombinedMonitor,
                        mergedweakrefs, false, dest.getLeafRef()));

        CodeExpr ifcond = CodeBinOpExpr.isNull(dest.getLeafRef());
        if (dest.getLeafRef().getType() instanceof CodeRVType.Interface) {
            // Additional check is needed because the destination
            // leaf may refer to a DisableHolder.
            CodeRVType.Interface itftype = (CodeRVType.Interface) dest
                    .getLeafRef().getType();
            CodeRVType.DisableHolder dhtype = itftype
                    .getDisableHolderType();
            CodeExpr extra = new CodeInstanceOfExpr(
                    dest.getLeafRef(), dhtype);
            ifcond = CodeBinOpExpr.logicalOr(ifcond, extra);
        }
        CodeStmtCollection ifbody = this
                .generateCopyStateInternalCode(sourceprms,
                        targetprms, dest, srcmonref, transition,
                        false);
        alivebody.add(new CodeConditionStmt(ifcond, ifbody));

        CodeExpr erase = new CodeMethodInvokeExpr(CodeType.foid(),
                source.getSetRef(), "eraseRange", numaliveref);
        stmts.add(new CodeExprStmt(erase));
        return stmts;
    }

    public CodeStmtCollection generateDefineNewCode(
            IndexingTreeQueryResult transition, Strategy strategy) {
        CodeStmtCollection stmts = new CodeStmtCollection();

        // It seems the original code assumes that defineNew:1--3 is not needed.
        // I hope that is correct assumption.

        CodeVarRefExpr monitorref;
        CodeExpr arg = null;
        if (strategy.needsTimeTracking) {
            arg = this.getTimestamp().generateGetAndIncrementCode();
        }
        MonitorCreationLazyCode create = new MonitorCreationLazyCode(
                this.getMonitorFeatures(), this.rvmSpec.getParameters(),
                transition.getWeakRefs(), this.getMonitorType(), arg);
        monitorref = create.getDeclaredMonitorRef();
        stmts.add(create);

        ((CustomMonitor.CustomMonitorFeatures) this.getMonitorFeatures()).addRelatedEvent(eventParams);
        stmts.add(this.getBehaviorObserver().generateNewMonitorCreatedCode(
                monitorref));

        MonitorInfo moninfo = this.getMonitorInfo();
        if (moninfo != null) {
            String legacycode = moninfo.newInfo(monitorref.getVariable()
                    .toLegacy(), this.eventParams);
            stmts.add(CodeStmtCollection.fromLegacy(legacycode));
        }

        stmts.add(this.generateInsertMonitorCode(transition, transition,
                monitorref, false, false));
        return stmts;
    }

    public CodeStmtCollection generateDisableUpdateCode(IndexingTreeQueryResult transition) {
        CodeStmtCollection stmts = new CodeStmtCollection();

        CodeVarRefExpr holderref;

        CodePair<CodeVarRefExpr> codepair = transition
                .generateFieldGetCode(IndexingTreeImplementation.Access.Leaf, "disableUpdated");
        stmts.add(codepair.getGeneratedCode());
        holderref = codepair.getLogicalReturn();

        if (!this.event.isStartEvent()) {
            // If this event is not a creation event, the matched entry can be
            // still null.
            // If that's the case, a DisableHolder should be inserted to keep
            // the 'disable' value.
            CodeExpr ifnull = CodeBinOpExpr.isNull(holderref);
            CodeStmtCollection ifbody = new CodeStmtCollection();
            stmts.add(new CodeConditionStmt(ifnull, ifbody));

            CodeRVType.DisableHolder dhtype;

            CodeRVType leaftype = transition.getEntry().getLeaf();
            if (leaftype instanceof CodeRVType.DisableHolder) {
                dhtype = (CodeRVType.DisableHolder) leaftype;
            } else if (leaftype instanceof CodeRVType.Interface) {
                CodeRVType.Interface itf = (CodeRVType.Interface) leaftype;
                dhtype = itf.getDisableHolderType();
            } else {
                throw new NotImplementedException();
            }

            CodeVarDeclStmt create = new CodeVarDeclStmt(new CodeVariable(
                    dhtype, "holder"), new CodeNewExpr(dhtype, CodeLiteralExpr.integer(-1)));
            CodeVarRefExpr createdref = new CodeVarRefExpr(create.getVariable());
            ifbody.add(create);
            ifbody.add(transition.generateLeafUpdateCode(createdref, false));

            CodeAssignStmt assign = new CodeAssignStmt(holderref, createdref);
            ifbody.add(assign);
        }

        CodeExpr ts = this.getTimestamp().generateGetAndIncrementCode();
        CodeMethodInvokeExpr invoke = new CodeMethodInvokeExpr(CodeType.foid(),
                holderref, "setDisable", ts);
        stmts.add(new CodeExprStmt(invoke));
        stmts.add(this.getBehaviorObserver().generateDisableFieldUpdatedCode(
                holderref));

        return stmts;
    }

    private IndexingTreeInterface findIndexingTreeForCopy(
            RVMonitorParameterPair needle) {
        for (RVMonitorParameterPair pair : indexingTreesForCopy.keySet()) {
            if (needle.equals(pair)) {
                return this.indexingTreesForCopy.get(pair);
            }
        }
        return null;
    }

    private IndexingTreeInterface findIndexingTree(RVMonitorParameterPair needle) {
        for (RVMParameters params : this.indexingTrees.keySet()) {
            if (params.equals(needle.getParam2())) {
                return this.indexingTrees.get(params);
            }
        }
        return null;
    }

    private IndexingTreeInterface findIndexingTree(RVMParameters needle) {
        for (RVMParameters params : this.indexingTrees.keySet()) {
            if (params.equals(needle)) {
                return this.indexingTrees.get(params);
            }
        }
        return null;
    }

    static class MonitorWeakRefSetLazyCode extends CodeLazyStmt {
        private final MonitorFeatures features;
        private final RVMParameters sourceprms;
        private final RVMParameters targetprms;
        private final WeakReferenceVariables weakrefs;
        private final CodeVarRefExpr monitorref;

        public MonitorWeakRefSetLazyCode(MonitorFeatures features,
                                         RVMParameters sourceprms, RVMParameters targetprms,
                                         WeakReferenceVariables weakrefs, CodeVarRefExpr monitorref) {
            this.features = features;
            this.sourceprms = sourceprms;
            this.targetprms = targetprms;
            this.weakrefs = weakrefs;
            this.monitorref = monitorref;
        }

        @Override
        protected CodeStmtCollection evaluate() {
            // The code can be generated only after the first pass has
            // completed.
            if (!this.features.isStabilized()) {
                return null;
            }

            CodeStmtCollection stmts = new CodeStmtCollection();

            if (this.features.isNonFinalWeakRefsInMonitorNeeded()
                    || this.features.isFinalWeakRefsInMonitorNeeded()) {
                for (RVMParameter prm : targetprms.setDiff(sourceprms)) {
                    CodeExpr rhs = new CodeVarRefExpr(weakrefs.getWeakRef(prm));
                    CodeExpr lhs = new CodeFieldRefExpr(monitorref,
                            CodeHelper.VariableName.getWeakRefInMonitor(prm,
                                    rhs.getType()));
                    CodeStmt assign = new CodeAssignStmt(lhs, rhs);
                    stmts.add(assign);
                }
            }

            return stmts;
        }
    }

    static class MonitorCreationLazyCode extends CodeLazyStmt {
        private final MonitorFeatures features;
        private final RVMParameters specParams;
        private final WeakReferenceVariables weakrefs;
        private final CodeType type;
        private final List<CodeExpr> fixedarguments;
        private final CodeVariable createdvar;

        public MonitorCreationLazyCode(MonitorFeatures features,
                                       RVMParameters specprms, WeakReferenceVariables weakrefs,
                                       CodeType type, CodeExpr... fixedarguments) {
            this.features = features;
            this.specParams = specprms;
            this.weakrefs = weakrefs;
            this.type = type;
            this.fixedarguments = new ArrayList<CodeExpr>();
            for (CodeExpr a : fixedarguments) {
                if (a == null) {
                    continue;
                }
                this.fixedarguments.add(a);
            }
            this.createdvar = new CodeVariable(this.type, "created");
        }

        public CodeVarRefExpr getDeclaredMonitorRef() {
            return new CodeVarRefExpr(this.createdvar);
        }

        @Override
        protected CodeStmtCollection evaluate() {
            // The code can be generated only after the first pass has
            // completed.
            if (!this.features.isStabilized()) {
                return null;
            }

            CodeStmtCollection stmts = new CodeStmtCollection();

            List<CodeExpr> args = new ArrayList<CodeExpr>();
            args.addAll(this.fixedarguments);
            if (this.features.isNonFinalWeakRefsInMonitorNeeded()
                    || this.features.isFinalWeakRefsInMonitorNeeded()) {
                // The constructor will expect one parameter for each. It's okay
                // to pass
                // null if that parameter is unavailable.
                for (RVMParameter param : this.specParams) {
                    CodeVariable wrvar = this.weakrefs.getWeakRef(param);
                    if (wrvar == null) {
                        args.add(CodeLiteralExpr.nul());
                    } else {
                        args.add(new CodeVarRefExpr(wrvar));
                    }
                }
            } else {
                for (RVMParameter param : this.features
                        .getRememberedParameters()) {
                    CodeVariable wrvar = this.weakrefs.getWeakRef(param);
                    args.add(new CodeVarRefExpr(wrvar));
                }
            }

            args.add(new CodeVarRefExpr(new CodeVariable(CodeType.integer(), "trie.root")));
            CodeVarDeclStmt decl = new CodeVarDeclStmt(this.createdvar,
                    new CodeNewExpr(this.type, args));
            stmts.add(decl);
            return stmts;
        }
    }

    private MonitorFeatures getMonitorFeatures() {
        return this.monitorClass.getFeatures();
    }

    private MonitorInfo getMonitorInfo() {
        return this.monitorClass.getMonitorInfo();
    }

    private Timestamp getTimestamp() {
        return Timestamp.create(rvmSpec.getName() + "_timestamp");
    }

    abstract static class Timestamp {
        protected CodeMemberField field;

        public static Timestamp create(String fieldname) {
            if (Main.options.finegrainedlock) {
                return new AtomicTimestamp(fieldname);
            }
            return new OrdinaryTimestamp(fieldname);
        }

        public abstract CodeExpr generateGetAndIncrementCode();
    }

    static class OrdinaryTimestamp extends Timestamp {
        OrdinaryTimestamp(String fieldname) {
            this.field = new CodeMemberField(fieldname, false, true, false,
                    CodeType.rong());
        }

        @Override
        public CodeExpr generateGetAndIncrementCode() {
            CodeFieldRefExpr fieldref = new CodeFieldRefExpr(this.field);
            return CodePrePostfixExpr.postfix(fieldref, true);
        }
    }

    static class AtomicTimestamp extends Timestamp {
        AtomicTimestamp(String fieldname) {
            CodeType fieldtype = CodeType.AtomicLong();
            CodeExpr init = new CodeNewExpr(fieldtype, CodeLiteralExpr.rong(1));
            this.field = new CodeMemberField(fieldname, false, true, true,
                    fieldtype, init);
        }

        @Override
        public CodeExpr generateGetAndIncrementCode() {
            CodeFieldRefExpr fieldref = new CodeFieldRefExpr(this.field);
            CodeExpr invoke = new CodeMethodInvokeExpr(CodeType.rong(),
                    fieldref, "getAndIncrement");
            return invoke;
        }
    }
}
