package io.gitlab.chaver.mining.patterns.measure.compute;

import io.gitlab.chaver.mining.patterns.io.Database;
import io.gitlab.chaver.mining.patterns.util.BitSetFacade;
import io.gitlab.chaver.mining.patterns.util.ConstraintSettings;
import org.chocosolver.solver.Model;

public abstract class CoverComputer extends MeasureComputer {

    private final BitSetFacade cover;
    protected final String type = ConstraintSettings.BITSET_TYPE;

    public CoverComputer(Database database, Model model) {
        super(database);
        cover = getBitSet(model);
    }

    public abstract BitSetFacade getBitSet(Model model);

    @Override
    public void compute(int i) {
        cover.and(i);
    }

    @Override
    public boolean isConstant(int i) {
        return cover.isSubsetOf(i);
    }

    @Override
    public boolean isConstant(int i, int j) {
        cover.resetMask();
        cover.andMask(j);
        return cover.maskIsSubsetOf(i);
    }

}
