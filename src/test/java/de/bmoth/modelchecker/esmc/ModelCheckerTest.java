package de.bmoth.modelchecker.esmc;

import de.bmoth.parser.ast.nodes.MachineNode;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static de.bmoth.TestParser.*;

public class ModelCheckerTest {
    @Test
    public void testAnySubstitution() {
        String machine = "MACHINE test \n";
        machine += "VARIABLES x,y \n";
        machine += "INVARIANT x:NATURAL & y : NATURAL \n";
        machine += "INITIALISATION x,y:= 1,2 \n";
        machine += "OPERATIONS\n";
        machine += "\treplaceBoth =\n";
        machine += "\t\tANY nVal \n";
        machine += "\t\tWHERE nVal:1..3\n";
        machine += "\t\tTHEN x,y := nVal,nVal\n";
        machine += "\tEND\n";
        machine += "END";

        ModelCheckingResult result = new ExplicitStateModelChecker(parseMachine(machine)).check();
        assertTrue(result.isCorrect());
        assertEquals(4, result.getNumberOfDistinctStatesVisited());
    }

    @Test
    public void testAnySubstitutionWithInvariantViolation() {
        String machine = "MACHINE test \n";
        machine += "VARIABLES x \n";
        machine += "INVARIANT x < 4 \n";
        machine += "INITIALISATION x := 1 \n";
        machine += "OPERATIONS\n";
        machine += "\treplaceX =\n";
        machine += "\t\tANY nVal \n";
        machine += "\t\tWHERE nVal > 0 & nVal < 5\n"; // should be < 4 to avoid
        // violation
        machine += "\t\tTHEN x := nVal\n";
        machine += "\tEND\n";
        machine += "END";

        ModelCheckingResult result = new ExplicitStateModelChecker(parseMachine(machine)).check();
        assertFalse(result.isCorrect());
    }

    @Test
    public void testSimpleMachineWithOperations() {
        String machine = "MACHINE SimpleMachine\n";
        machine += "VARIABLES x\n";
        machine += "INVARIANT x : NATURAL & x >= 0 & x <= 2\n";
        machine += "INITIALISATION x := 0\n";
        machine += "OPERATIONS\n";
        machine += "\tInc = SELECT x < 2 THEN x := x + 1 END;\n";
        machine += "\tDec = SELECT x > 0 THEN x := x - 1 END\n";
        machine += "END";

        ModelCheckingResult result = new ExplicitStateModelChecker(parseMachine(machine)).check();
        assertTrue(result.isCorrect());
        assertEquals(3, result.getNumberOfDistinctStatesVisited());
    }

    @Test
    public void testSimpleMachineWithOperations2() {
        String machine = "MACHINE SimpleMachine\n";
        machine += "VARIABLES x\n";
        machine += "INVARIANT x : NATURAL & x >= 0 & x <= 2\n";
        machine += "INITIALISATION x := 0\n";
        machine += "OPERATIONS\n";
        machine += "\tBlockSubstitution = BEGIN x := x + 1 END\n";
        machine += "END";

        ModelCheckingResult result = new ExplicitStateModelChecker(parseMachine(machine)).check();
        // the operation BlockSubstitution will finally violate the invariant
        // x<=2
        assertFalse(result.isCorrect());
        assertEquals(4, result.getNumberOfDistinctStatesVisited());
    }

    @Test
    public void testAbort() {
        String machine = "MACHINE SimpleMachine\n";
        machine += "VARIABLES x\n";
        machine += "INVARIANT x : INTEGER\n";
        machine += "INITIALISATION x := 0\n";
        machine += "OPERATIONS\n";
        machine += "\tinc = BEGIN x := x + 1 END\n";
        machine += "END";

        MachineNode machineAsSemanticAst = parseMachine(machine);
        ExplicitStateModelChecker modelChecker = new ExplicitStateModelChecker(machineAsSemanticAst);

        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            modelChecker.abort();
        }).start();

        ModelCheckingResult result = modelChecker.check();

        assertFalse(result.isCorrect());
        assertEquals("aborted", result.getMessage());
    }

    @Test
    public void testEnumeratedSet() {
        String machine = "MACHINE SimpleMachine\n";
        machine += "SETS set={s1,s2,s3} \n";
        machine += "VARIABLES x\n";
        machine += "INVARIANT x: set & x = s2 \n";
        machine += "INITIALISATION x := s1 \n";
        machine += "END";

        ModelCheckingResult result = new ExplicitStateModelChecker(parseMachine(machine)).check();
        // the initialisation will finally violate the invariant x = s2
        assertFalse(result.isCorrect());
        assertEquals(1, result.getNumberOfDistinctStatesVisited());
    }

    @Test
    @Ignore("Z3 finds an empty model for this test?!")
    public void testDeferredSet() {
        String machine = "MACHINE SimpleMachine\n";
        machine += "SETS set\n";
        machine += "VARIABLES x,y\n";
        machine += "INVARIANT x : set & y : set & x = y\n";
        machine += "INITIALISATION x :: set || y :: set \n";
        machine += "END";
        ModelCheckingResult result = new ExplicitStateModelChecker(parseMachine(machine)).check();
        // the initialisation will finally violate the invariant x = y
        assertFalse(result.isCorrect());
        assertEquals(1, result.getNumberOfDistinctStatesVisited());
    }

    @Test
    public void testDeferredSetUsingAny() {
        String machine = "MACHINE SimpleMachine\n";
        machine += "SETS set\n";
        machine += "VARIABLES x,y\n";
        machine += "INVARIANT x : set & y : set & x = y\n";
        machine += "INITIALISATION ANY a,b WHERE a:set & b:set THEN x,y:=a,b END\n";
        machine += "END";
        ModelCheckingResult result = new ExplicitStateModelChecker(parseMachine(machine)).check();
        // the initialisation will finally violate the invariant x = y
        assertFalse(result.isCorrect());
    }
}