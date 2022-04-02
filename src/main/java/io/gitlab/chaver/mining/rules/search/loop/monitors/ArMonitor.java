package io.gitlab.chaver.mining.rules.search.loop.monitors;

import io.gitlab.chaver.mining.patterns.io.Database;
import io.gitlab.chaver.mining.rules.io.AssociationRule;
import lombok.AllArgsConstructor;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.LinkedList;
import java.util.List;

/**
 * Class used for storing association rules x -> y
 */
@AllArgsConstructor
public class ArMonitor implements IMonitorSolution {

    private Database database;
    private BoolVar[] x;
    private BoolVar[] y;
    private IntVar freqX;
    private IntVar freqY;
    private IntVar freqZ;
    private final List<AssociationRule> associationRules = new LinkedList<>();

    private int[] getPattern(BoolVar[] p) {
        List<Integer> pattern = new LinkedList<>();
        for (int i = 0; i < p.length; i++) {
            if (p[i].isInstantiatedTo(1)) pattern.add(database.getItems()[i]);
        }
        return pattern.stream().mapToInt(i -> i).toArray();
    }

    @Override
    public void onSolution() {
        associationRules.add(
                new AssociationRule(
                        getPattern(x), getPattern(y), freqX.getValue(), freqY.getValue(), freqZ.getValue()
                )
        );
    }

    public List<AssociationRule> getAssociationRules() {
        return associationRules;
    }
}
