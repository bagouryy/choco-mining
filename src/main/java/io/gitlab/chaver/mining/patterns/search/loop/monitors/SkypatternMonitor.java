/*
 * This file is part of io.gitlab.chaver:data-mining
 *
 * Copyright (c) 2022, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
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
