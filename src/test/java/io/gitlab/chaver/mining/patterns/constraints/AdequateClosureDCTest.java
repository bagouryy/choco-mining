/*
 * This file is part of io.gitlab.chaver:data-mining (https://gitlab.com/chaver/data-mining)
 *
 * Copyright (c) 2023, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.constraints;

import io.gitlab.chaver.mining.patterns.io.Database;
import io.gitlab.chaver.mining.patterns.measure.Measure;
import org.chocosolver.solver.variables.BoolVar;

import java.util.List;

public class AdequateClosureDCTest extends AdequateClosureTest {

    @Override
    protected AdequateClosure getAdequateClosure(Database database, List<Measure> measures, BoolVar[] x) {
        return new AdequateClosureDC(database, measures, x);
    }
}
