package de.bmoth.typechecker;

import org.junit.Test;

import java.util.HashMap;

import static de.bmoth.typechecker.TestTypechecker.getFormulaTypes;
import static org.junit.Assert.assertEquals;

public class SetFormulaTest {

    @Test
    public void testOverwriteRelation() throws Exception {
        String formula = "k = {x|->2} <+ {1|->y}";
        HashMap<String, String> formulaTypes = getFormulaTypes(formula);
        assertEquals("POW(INTEGER*INTEGER)", formulaTypes.get("k"));
        assertEquals("INTEGER", formulaTypes.get("x"));
        assertEquals("INTEGER", formulaTypes.get("y"));
    }

    @Test
    public void testDomainRestriction() throws Exception {
        String formula = "k = x <| {1|->1}";
        HashMap<String, String> formulaTypes = getFormulaTypes(formula);
        assertEquals("POW(INTEGER*INTEGER)", formulaTypes.get("k"));
        assertEquals("POW(INTEGER)", formulaTypes.get("x"));
    }

    @Test
    public void testDomainSubstraction() throws Exception {
        String formula = "k = x <<| {1|->1}";
        HashMap<String, String> formulaTypes = getFormulaTypes(formula);
        assertEquals("POW(INTEGER*INTEGER)", formulaTypes.get("k"));
        assertEquals("POW(INTEGER)", formulaTypes.get("x"));
    }

    @Test
    public void testRangeRestriction() throws Exception {
        String formula = "k = {1|->1} |> x";
        HashMap<String, String> formulaTypes = getFormulaTypes(formula);
        assertEquals("POW(INTEGER*INTEGER)", formulaTypes.get("k"));
        assertEquals("POW(INTEGER)", formulaTypes.get("x"));
    }

    @Test
    public void testRangeSubstraction() throws Exception {
        String formula = "k = {1|->1} |>> x";
        HashMap<String, String> formulaTypes = getFormulaTypes(formula);
        assertEquals("POW(INTEGER*INTEGER)", formulaTypes.get("k"));
        assertEquals("POW(INTEGER)", formulaTypes.get("x"));
    }

    @Test
    public void testInclusion() throws Exception {
        String formula = "x <: {1}";
        HashMap<String, String> formulaTypes = getFormulaTypes(formula);
        assertEquals("POW(INTEGER)", formulaTypes.get("x"));
    }

    @Test
    public void testNonInclusion() throws Exception {
        String formula = "{x} <: {1}";
        HashMap<String, String> formulaTypes = getFormulaTypes(formula);
        assertEquals("INTEGER", formulaTypes.get("x"));
    }

    @Test
    public void testStrictInclusion() throws Exception {
        String formula = "{1} <<: x";
        HashMap<String, String> formulaTypes = getFormulaTypes(formula);
        assertEquals("POW(INTEGER)", formulaTypes.get("x"));
    }

    @Test
    public void testStrictNonInclusion() throws Exception {
        String formula = "{1} /<<: {x}";
        HashMap<String, String> formulaTypes = getFormulaTypes(formula);
        assertEquals("INTEGER", formulaTypes.get("x"));
    }

    @Test
    public void testQuantifiedUnion() throws Exception {
        String formula = "x = UNION(a).(a : 1..10 | {a|->a}) ";
        HashMap<String, String> formulaTypes = getFormulaTypes(formula);
        assertEquals("POW(INTEGER*INTEGER)", formulaTypes.get("x"));
    }

    @Test
    public void testGeneralizedUnion() throws Exception {
        String formula = "a = union({{1},{b},c}\\/d)";
        HashMap<String, String> formulaTypes = getFormulaTypes(formula);
        assertEquals("POW(INTEGER)", formulaTypes.get("a"));
        assertEquals("INTEGER", formulaTypes.get("b"));
        assertEquals("POW(INTEGER)", formulaTypes.get("c"));
        assertEquals("POW(POW(INTEGER))", formulaTypes.get("d"));
    }

}
