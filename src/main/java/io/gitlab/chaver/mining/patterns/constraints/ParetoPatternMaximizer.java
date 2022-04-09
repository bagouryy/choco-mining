package io.gitlab.chaver.mining.patterns.constraints;

import io.gitlab.chaver.mining.patterns.io.Pattern;
import io.gitlab.chaver.mining.patterns.search.loop.monitors.PatternSearchMonitor;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import java.util.List;

/**
 * Pareto global constraint : Ensures that next solution is not dominated by any pattern in the archive
 */
public class ParetoPatternMaximizer extends Propagator<IntVar> {

    private IntVar[] objectives;
    private List<Pattern> archive;

    public ParetoPatternMaximizer(IntVar[] objectives, PatternSearchMonitor monitor) {
        super(objectives);
        this.objectives = objectives;
        this.archive = monitor.getPatterns();
    }

    /**
     * Compute dominated point for objective i,
     *  i.e. DP_i = (obj_1_max,...,obj_i_min,...,obj_m_max)
     * @param i index of the variable
     * @return dominated point
     */
    private int[] computeDominatedPoint(int i) {
        int[] DP = new int[objectives.length];
        for (int j = 0; j < objectives.length; j++) {
            IntVar currentVar = objectives[j];
            DP[j] = j == i ? currentVar.getLB() : currentVar.getUB();
        }
        return DP;
    }

    /**
     * Compute tightest point for objective i
     *  i.e. the point that dominates DP_i and has the biggest obj_i
     * @param i index of the variable
     */
    private void computeTightestPoint(int i) throws ContradictionException {
        boolean tightestPointFound = false;
        int tightestPoint = 0;
        int[] dominatedPoint = computeDominatedPoint(i);
        for (Pattern p : archive) {
            int[] sol = p.getMeasures();
            int dominates = dominates(sol, dominatedPoint, i);
            if (dominates > 0) {
                int currentPoint = dominates == 1 ? sol[i] : sol[i] + 1;
                if (! tightestPointFound || tightestPoint < currentPoint) {
                    tightestPointFound = true;
                    tightestPoint = currentPoint;
                }
            }
        }
        if (tightestPointFound) {
            objectives[i].updateLowerBound(tightestPoint, this);
        }
    }

    /**
     * Return an int :
     *  0 if a doesn't dominate b
     *  1 if a dominates b and a = b if we don't take into account index i
     *  2 if a dominates b and a dominates b if we don't take into account index i
     * @param a vector
     * @param b vector
     * @param i index
     * @return an int representing the fact that a dominates b
     */
    private int dominates(int[] a, int[] b, int i) {
        int dominates = 0;
        for (int j = 0; j < objectives.length; j++) {
            if (a[j] < b[j]) return 0;
            if (a[j] > b[j]) {
                if (dominates == 0) dominates = 1;
                if (j != i) dominates = 2;
            }
        }
        return dominates;
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < objectives.length; i++) {
            computeTightestPoint(i);
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }
}
