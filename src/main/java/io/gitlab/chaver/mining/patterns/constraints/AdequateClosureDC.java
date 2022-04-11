package io.gitlab.chaver.mining.patterns.constraints;

import io.gitlab.chaver.mining.patterns.io.Database;
import io.gitlab.chaver.mining.patterns.measure.Measure;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;

import java.util.List;

public class AdequateClosureDC extends AdequateClosure {

    public AdequateClosureDC(Database database, List<Measure> measures, BoolVar[] items) {
        super(database, measures, items);
    }

    @Override
    public void checkDC() throws ContradictionException {
        rule3();
    }
}
