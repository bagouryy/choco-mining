/*
 * This file is part of io.gitlab.chaver:data-mining (https://gitlab.com/chaver/data-mining)
 *
 * Copyright (c) 2023, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.examples;

import io.gitlab.chaver.mining.patterns.constraints.CoverClosure;
import io.gitlab.chaver.mining.patterns.constraints.CoverSize;
import io.gitlab.chaver.mining.patterns.constraints.Generator;
import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.Database;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.chocosolver.solver.search.strategy.Search.intVarSearch;

/**
 * Example of Minimal Non-Redundant association rule mining
 */
public class ExampleMNRsMining {

    static int[] getItemset(BoolVar[] x, Database database) {
        return IntStream
                .range(0, x.length)
                .filter(i -> x[i].getValue() == 1)
                .map(i -> database.getItems()[i])
                .toArray();
    }

    public static void main(String[] args) throws Exception {
        String dataPath = "src/test/resources/contextPasquier99/contextPasquier99.dat";
        Database database = new DatReader(dataPath).readFiles();
        int minFreq = 1;
        int minConf = 20;
        Model model = new Model("MNRs mining");
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        BoolVar[] y = model.boolVarArray("y", database.getNbItems());
        BoolVar[] z = model.boolVarArray("z", database.getNbItems());
        for (int i = 0; i < database.getNbItems(); i++) {
            model.arithm(x[i], "+", y[i], "<=", 1).post();
            model.addClausesBoolOrEqVar(x[i], y[i], z[i]);
        }
        model.addClausesBoolOrArrayEqualTrue(x);
        model.addClausesBoolOrArrayEqualTrue(y);
        IntVar freqZ = model.intVar("freqZ", minFreq, database.getNbTransactions());
        new Constraint("frequent Z", new CoverSize(database, freqZ, z)).post();
        IntVar freqX = model.intVar("freqX", minFreq, database.getNbTransactions());
        new Constraint("frequent X", new CoverSize(database, freqX, x)).post();
        freqZ.mul(100).ge(freqX.mul(minConf)).post();
        new Constraint("generator x", new Generator(database, x))
                .post();
        new Constraint("closed z", new CoverClosure(database, z)).post();
        Solver solver = model.getSolver();
        solver.setSearch(intVarSearch(
                new InputOrder<>(model),
                new IntDomainMin(),
                ArrayUtils.append(x, y, z)
        ));
        System.out.println("List of the MNRs with a min freq=1 and a min confidence=0.2");
        while (solver.solve()) {
            double conf = (double) freqZ.getValue() / freqX.getValue();
            System.out.println(Arrays.toString(getItemset(x, database)) + " => " +
                    Arrays.toString(getItemset(y, database)) + ", freq=" + freqZ.getValue() + ", conf=" + conf);
        }
    }
}
