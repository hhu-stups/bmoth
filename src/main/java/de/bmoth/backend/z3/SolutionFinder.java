package de.bmoth.backend.z3;

import com.microsoft.z3.*;
import de.bmoth.backend.Abortable;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class SolutionFinder implements Abortable {
    private final Solver solver;
    private final Context z3Context;
    private boolean isAborted;

    /**
     * Solution finder expects the constraint to be already added to the
     * corresponding solver
     *
     * @param solver    corresponding z3 solver
     * @param z3Context corresponding z3 context
     */
    public SolutionFinder(Solver solver, Context z3Context) {
        this.solver = solver;
        this.z3Context = z3Context;
    }

    /**
     * Evaluate a single solution from solver over all variables, constraints
     * have to be satisfiable!
     *
     * @param model current model to find a solution in
     * @return a solution
     */
    private BoolExpr findSolution(Model model) {
        FuncDecl[] constants = model.getConstDecls();

        BoolExpr result = null;
        for (FuncDecl var : constants) {
            if (var.getName().toString().contains("!")) {
                continue;
            }
            Expr value = model.eval(var.apply(), true);

            if (result == null) {
                result = z3Context.mkEq(var.apply(), value);
            } else {
                result = z3Context.mkAnd(result, z3Context.mkEq(var.apply(), value));
            }
        }
        return result;
    }

    /**
     * Evaluates all solutions up to a given maximum of iterations
     * <p>
     * credit goes to Taylor
     *
     * @param constraint    the constraint to find solutions for
     * @param maxIterations the maximum nr of iterations
     * @return list of found solution
     * @see <a href=
     * "http://stackoverflow.com/questions/13395391/z3-finding-all-satisfying-models#answer-13398853">Taylor's
     * answer on so.com</a>
     */
    public Set<Model> findSolutions(BoolExpr constraint, int maxIterations) {
        isAborted = false;
        Set<Model> result = new HashSet<>();

        // create a solution finding scope to not pollute original one
        solver.push();
        solver.add(constraint);

        // as long as formula is satisfiable:
        for (int i = 0; !isAborted && solver.check() == Status.SATISFIABLE && i < maxIterations; i++) {
            Model currentModel = solver.getModel();

            // find a solution ...
            BoolExpr solution = findSolution(currentModel);

            // prevent continuing when formula is SAT but model is empty
            if (solution == null) {
                break;
            }

            // ... and add it as an exclusion constraint to solver stack
            solver.add(z3Context.mkNot(solution));

            result.add(currentModel);
        }

        // delete solution finding scope to remove all exclusion constraints
        // from solver stack
        solver.pop();

        return result;
    }

    @Override
    public void abort() {
        isAborted = true;
    }
}
