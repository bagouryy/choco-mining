package io.gitlab.chaver.mining.patterns.measure.compute;

import io.gitlab.chaver.mining.patterns.io.Database;
import io.gitlab.chaver.mining.patterns.util.BitSetFacade;
import io.gitlab.chaver.mining.patterns.util.BitSetFactory;
import org.chocosolver.solver.Model;

public class FreqComputer extends CoverComputer {

    public FreqComputer(Database database, Model model) {
        super(database, model);
    }

    @Override
    public BitSetFacade getBitSet(Model model) {
        return BitSetFactory.getBitSet(type, database, model);
    }
}
