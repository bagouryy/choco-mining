package io.gitlab.chaver.mining.patterns.measure.compute;

import io.gitlab.chaver.mining.patterns.io.Database;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Model;

public abstract class IntMeasureComputer extends MeasureComputer {

    protected IStateInt value;

    public IntMeasureComputer(Database database, Model model) {
        super(database);
        this.value = model.getEnvironment().makeInt(getInitValue());
    }

    public abstract int getInitValue();
}
