package io.gitlab.chaver.mining.patterns.measure.compute;

import io.gitlab.chaver.mining.patterns.io.Database;
import org.chocosolver.solver.Model;

public class MeanValComputer extends MeasureComputer {

    private MinValComputer minValComputer;
    private MaxValComputer maxValComputer;

    public MeanValComputer(Database database, Model model, int num) {
        super(database);
        this.minValComputer = new MinValComputer(database, model, num);
        this.maxValComputer = new MaxValComputer(database, model, num);
    }

    @Override
    public void compute(int i) {
        minValComputer.compute(i);
        maxValComputer.compute(i);
    }

    @Override
    public boolean isConstant(int i) {
        return minValComputer.isConstant(i) && maxValComputer.isConstant(i);
    }

    @Override
    public boolean isConstant(int i, int j) {
        return minValComputer.isConstant(i, j) && maxValComputer.isConstant(i, j);
    }
}
