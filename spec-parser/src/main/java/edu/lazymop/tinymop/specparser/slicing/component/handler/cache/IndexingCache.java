// Based on JavaMOP's com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingCacheNew

package edu.lazymop.tinymop.specparser.slicing.component.handler.cache;

import java.util.Map;
import java.util.TreeMap;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.CodeVarRefExpr;
import com.runtimeverification.rvmonitor.java.rvj.output.codedom.helper.CodeHelper;
import com.runtimeverification.rvmonitor.java.rvj.output.combinedoutputcode.newindexingtree.IndexingTreeInterface;
import com.runtimeverification.rvmonitor.java.rvj.parser.ast.rvmspec.RVMParameter;

public class IndexingCache {

    // Map parameter variable to cache variable. e.g., {col: col_iter_Map_cachekey_col}
    private final TreeMap<RVMParameter, String> keys;
    private final String cacheKeyValue; // The variable that holds the cache. (iter_Map_cachevalue/col_iter_Map_cachevalue)

    private IndexingCache(TreeMap<RVMParameter, String> keys, String cacheKeyValue) {
        this.keys = keys;
        this.cacheKeyValue = cacheKeyValue;

        this.validate();
    }

    private void validate() {
        if (this.keys == null) {
            throw new IllegalArgumentException();
        }
        if (this.cacheKeyValue == null) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Creates an IndexingCache from the given tree name and indexing tree interface.
     *
     * @param treeName the name of the tree
     * @param itf the IndexingTreeInterface representing the indexing tree
     * @return the created IndexingCache
     */
    public static IndexingCache fromTree(String treeName, IndexingTreeInterface itf) {
        TreeMap<RVMParameter, String> keys = new TreeMap<>();
        for (RVMParameter key : itf.getQueryParams()) {
            String cacheKeyValue = CodeHelper.VariableName.getIndexingTreeCacheKeyName(treeName, key);
            keys.put(key, cacheKeyValue);
        }

        if (keys.isEmpty()) {
            return null;
        }

        String cacheKeyValue = CodeHelper.VariableName.getIndexingTreeCacheValueName(treeName);
        return new IndexingCache(keys, cacheKeyValue);
    }

    /**
     * Generates the cache hit condition.
     *
     * @return the BinaryExpr representing the cache condition
     */
    public BinaryExpr getCacheCondition() {
        BinaryExpr currentCondition = null;
        for (Map.Entry<RVMParameter, String> entry : this.keys.entrySet()) {
            BinaryExpr isEqual = new BinaryExpr(new NameExpr(entry.getKey().getName()), new NameExpr(entry.getValue()),
                    BinaryExpr.Operator.EQUALS);

            if (currentCondition == null) {
                currentCondition = isEqual;
            } else {
                currentCondition = new BinaryExpr(currentCondition, isEqual, BinaryExpr.Operator.AND);
            }
        }
        return currentCondition;
    }

    /**
     * Generates the cache retrieval code and adds it to the given block statement.
     *
     * @param destref the CodeVarRefExpr representing the destination reference
     * @return the BlockStmt containing the cache retrieval code
     */
    public BlockStmt getCacheRetrievalCode(CodeVarRefExpr destref) {
        BlockStmt blockStmt = new BlockStmt();

        blockStmt.addStatement(new AssignExpr(new NameExpr(destref.getVariable().getName()),
                new NameExpr(cacheKeyValue), AssignExpr.Operator.ASSIGN
        ));

        blockStmt.addStatement(new AssignExpr(new NameExpr("cachehit"), new BooleanLiteralExpr(true),
                AssignExpr.Operator.ASSIGN
        ));
        return blockStmt;
    }

    /**
     * Generates the cache update code and adds it to the given block statement.
     * Only update cache if cachehit is false
     *
     * @param destref the CodeVarRefExpr representing the destination reference
     * @return the BlockStmt containing the cache update code
     */
    public BlockStmt getCacheUpdateCode(CodeVarRefExpr destref) {
        BlockStmt blockStmt = new BlockStmt();
        for (Map.Entry<RVMParameter, String> entry : this.keys.entrySet()) {
            blockStmt.addStatement(new AssignExpr(new NameExpr(entry.getValue()), new NameExpr(entry.getKey().getName()),
                    AssignExpr.Operator.ASSIGN
            ));
        }
        blockStmt.addStatement(new AssignExpr(new NameExpr(cacheKeyValue), new NameExpr(destref.getVariable().getName()),
                AssignExpr.Operator.ASSIGN));
        return blockStmt;
    }
}
