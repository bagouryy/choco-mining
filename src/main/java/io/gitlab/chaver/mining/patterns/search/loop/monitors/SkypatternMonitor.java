package io.gitlab.chaver.mining.patterns.search.loop.monitors;

import io.gitlab.chaver.chocotools.objective.IntParetoMaximizer;
import io.gitlab.chaver.chocotools.util.Creator;
import io.gitlab.chaver.mining.patterns.io.Pattern;
import org.chocosolver.solver.variables.IntVar;

public class SkypatternMonitor extends IntParetoMaximizer<Pattern> {

    public SkypatternMonitor(IntVar[] objectives, Creator<Pattern> creator) {
        super(objectives, creator);
    }

    @Override
    protected int[] getObjValues(Pattern sol) {
        return sol.getMeasures();
    }
}
