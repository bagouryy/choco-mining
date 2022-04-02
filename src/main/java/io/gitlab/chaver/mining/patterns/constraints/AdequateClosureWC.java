package io.gitlab.chaver.mining.patterns.constraints;

import io.gitlab.chaver.mining.patterns.io.Database;
import io.gitlab.chaver.mining.patterns.measure.Measure;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;


import java.util.List;

public class AdequateClosureWC extends AdequateClosure {

    public AdequateClosureWC(Database database, BoolVar[] items, List<Measure> measures) {
        super(database, items, measures);
    }

    @Override
    public void checkDC() throws ContradictionException {}
}
