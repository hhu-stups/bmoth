package de.bmoth.modelchecker.esmc;

import com.microsoft.z3.Expr;
import de.bmoth.TestUsingZ3;
import de.bmoth.modelchecker.State;
import org.junit.Test;

import java.util.HashMap;

import static net.trajano.commons.testing.EqualsTestUtil.assertEqualsImplementedCorrectly;
import static org.junit.Assert.*;

public class StateTest extends TestUsingZ3 {
    @Test
    public void testToString() throws Exception {
        HashMap<String, Expr> map = new HashMap<>();

        map.put("z", z3Context.mkInt(13));
        map.put("x", z3Context.mkInt(11));
        map.put("y", z3Context.mkInt(12));
        map.put("a", z3Context.mkInt(-200));

        State state = new State(map);
        assertEquals("{a=-200, x=11, y=12, z=13}", state.toString());
    }

    @Test
    public void testStateEquals() throws Exception {
        HashMap<String, Expr> map1 = new HashMap<>();
        HashMap<String, Expr> map2 = new HashMap<>();

        map1.put("x", z3Context.mkInt(11));
        map2.put("x", z3Context.mkInt(11));

        State state1 = new State(map1);
        State state2 = new State(map2);
        assertEquals(state1, state2);
        assertEquals(state1.hashCode(), state2.hashCode());
    }

    @Test
    public void testStateEquals2() throws Exception {
        HashMap<String, Expr> map1 = new HashMap<>();
        HashMap<String, Expr> map2 = new HashMap<>();

        map1.put("x", z3Context.mkInt(11));
        map1.put("y", z3Context.mkInt(12));
        map2.put("x", z3Context.mkInt(11));

        State state1 = new State(map1);
        State state2 = new State(map2);
        assertNotEquals(state1, state2);
        assertNotEquals(state1.hashCode(), state2.hashCode());
    }

    @Test
    public void testStateEquals3() throws Exception {
        HashMap<String, Expr> map = new HashMap<>();

        map.put("x", z3Context.mkInt(11));

        State state = new State(map);
        assertEquals(state, state);
        assertNotEquals(state, new Object());
    }

    @Test
    public void testGetStateConstraint() {
        HashMap<String, Expr> map1 = new HashMap<>();

        map1.put("x", z3Context.mkInt(11));
        map1.put("y", z3Context.mkInt(12));

        State state1 = new State(map1);
        State state2 = new State(new HashMap<>());

        assertEquals("(and (= x 11) (= y 12))", state1.getStateConstraint(z3Context).toString());
        assertNull(state2.getStateConstraint(z3Context));
    }

    @Test
    public void testEquals() {
        HashMap<String, Expr> map = new HashMap<>();

        map.put("a", z3Context.mkInt(1));
        map.put("b", z3Context.mkInt(2));

        State s = new State(map);
        State s2 = new State(map);

        assertEqualsImplementedCorrectly(s);
        assertEqualsImplementedCorrectly(s, s2);

        s2 = new State(map);
        State s3 = new State(map);
        assertEqualsImplementedCorrectly(s2);
        assertEqualsImplementedCorrectly(s2, s3);
    }
}
