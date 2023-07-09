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

import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.Database;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OverlapTest {

    @Test
    void test() throws Exception {
        Database database = new DatReader("src/test/resources/iris/iris.dat").readFiles();
        Model model = new Model("Diversity");
        int theta = (int) Math.round(database.getNbTransactions() * 0.01d);
        IntVar freq = model.intVar("freq", theta, database.getNbTransactions());
        IntVar length = model.intVar("length", 1, database.getNbItems());
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        model.sum(x, "=", length).post();
        model.post(new Constraint("Cover Size", new CoverSize(database, freq, x)));
        model.post(new Constraint("Cover Closure", new CoverClosure(database, x)));
        double jmax = 0.05;
        Overlap overlap = new Overlap(database, x, 0.05, theta);
        model.post(new Constraint("Overlap", overlap));
        Solver solver = model.getSolver();
        solver.plugMonitor(overlap);
        solver.setSearch(Search.intVarSearch(
                new InputOrder<>(model),
                new IntDomainMin(),
                x
        ));
        while (solver.solve());
        List<BitSet> covers = overlap.getCoversHistory();
        for (BitSet cover : covers) {
            for (BitSet cover2 : covers) {
                if (!cover.equals(cover2)) {
                    double jaccard = Overlap.computeJaccard(cover, cover2);
                    assertTrue(jaccard <= jmax);
                }
            }
        }
    }

}