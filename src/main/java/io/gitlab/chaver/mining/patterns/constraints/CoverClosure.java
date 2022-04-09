package io.gitlab.chaver.mining.patterns.constraints;

import io.gitlab.chaver.mining.patterns.io.Database;
import org.chocosolver.solver.variables.BoolVar;

import java.util.Arrays;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.freq;


/**
 * Given a set of boolean variables x, ensures that x is a closed pattern w.r.t. {freq}
 * Fore more information, see Schaus et al. - CoverSize : A global constraint for frequency-based itemset mining
 */
public class CoverClosure extends AdequateClosureWC {

    public CoverClosure(Database database, BoolVar[] items) {
        super(database, items, Arrays.asList(freq()));
    }
}
